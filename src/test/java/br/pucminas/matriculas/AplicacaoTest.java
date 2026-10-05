package br.pucminas.matriculas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AplicacaoTest {

    @TempDir
    Path diretorio;

    @Test
    void persisteMatriculaCobrancaEConsultaDoProfessor() {
        Aplicacao aplicacao = new Aplicacao(diretorio);
        assertTrue(aplicacao.isPrimeiroAcesso());
        assertNull(aplicacao.autenticar("joao", "errada"));

        Aluno joao = assertInstanceOf(Aluno.class, aplicacao.autenticar("joao", "senha123"));
        Semestre semestre = aplicacao.semestresAbertos().get(0);
        Disciplina projeto = disciplina(aplicacao, "ES001");
        aplicacao.matricularObrigatoria(joao, projeto, semestre);

        assertFalse(aplicacao.getCobranca().getRegistros().isEmpty());
        assertTrue(aplicacao.getCobranca().getRegistros().get(0).contains("ES001"));

        Aplicacao recarregada = new Aplicacao(diretorio);
        assertFalse(recarregada.isPrimeiroAcesso());
        Aluno joaoSalvo = assertInstanceOf(Aluno.class, recarregada.autenticar("joao", "senha123"));
        assertEquals(1, joaoSalvo.getMatriculas().size());
        assertEquals("ES001", joaoSalvo.getMatriculas().get(0).getDisciplina().getCodigo());
        assertEquals(1, recarregada.getCobranca().getRegistros().size());

        Professor ana = professor(recarregada, "ana");
        assertEquals(1, ana.listarAlunos(joaoSalvo.getMatriculas().get(0).getDisciplina()).size());
        Professor carlos = assertInstanceOf(Professor.class, recarregada.autenticar("carlos", "senha123"));
        org.junit.jupiter.api.Assertions.assertThrows(RegraNegocioException.class,
                () -> carlos.listarAlunos(joaoSalvo.getMatriculas().get(0).getDisciplina()));
    }

    @Test
    void ofertaCanceladaNaoImpedeOSemestreSeguinteDepoisDeRecarregar() {
        Aplicacao aplicacao = new Aplicacao(diretorio);
        Aluno joao = assertInstanceOf(Aluno.class, aplicacao.autenticar("joao", "senha123"));
        Aluno maria = assertInstanceOf(Aluno.class, aplicacao.autenticar("maria", "senha123"));
        Semestre semestre = aplicacao.semestresAbertos().get(0);
        Disciplina projeto = disciplina(aplicacao, "ES001");
        aplicacao.matricularObrigatoria(joao, projeto, semestre);
        aplicacao.matricularObrigatoria(maria, projeto, semestre);
        aplicacao.encerrarPeriodo(semestre.getPeriodoMatricula());

        Semestre seguinte = aplicacao.abrirSemestre(2027, 1);
        aplicacao.gerarCurriculo(seguinte, java.util.List.of(projeto));
        aplicacao.definirPeriodo(seguinte, LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));

        Aplicacao recarregada = new Aplicacao(diretorio);
        Semestre seguinteSalvo = semestre(recarregada, 2027, 1);
        Disciplina projetoSalvo = disciplina(recarregada, "ES001");
        Aluno pedro = assertInstanceOf(Aluno.class, recarregada.autenticar("pedro", "senha123"));
        recarregada.matricularObrigatoria(pedro, projetoSalvo, seguinteSalvo);

        assertEquals(1, projetoSalvo.obterQuantidadeInscritos(seguinteSalvo));
        assertEquals(Disciplina.CANCELADA, semestre(recarregada, 2026, 2).situacaoNaOferta(projetoSalvo));
        assertEquals(Disciplina.EM_INSCRICAO, seguinteSalvo.situacaoNaOferta(projetoSalvo));
        Professor ana = professor(recarregada, "ana");
        assertEquals(3, ana.listarAlunos(projetoSalvo).size());
    }

    private Disciplina disciplina(Aplicacao aplicacao, String codigo) {
        for (Disciplina disciplina : aplicacao.getSecretaria().consultarDisciplinas()) {
            if (disciplina.getCodigo().equals(codigo)) {
                return disciplina;
            }
        }
        throw new AssertionError("Disciplina não encontrada: " + codigo);
    }

    private Professor professor(Aplicacao aplicacao, String login) {
        for (Professor professor : aplicacao.getSecretaria().getProfessores()) {
            if (professor.getLogin().equals(login)) {
                return professor;
            }
        }
        throw new AssertionError("Professor não encontrado: " + login);
    }

    private Semestre semestre(Aplicacao aplicacao, int ano, int periodo) {
        for (Semestre semestre : aplicacao.getSecretaria().getSemestres()) {
            if (semestre.getAno() == ano && semestre.getPeriodo() == periodo) {
                return semestre;
            }
        }
        throw new AssertionError("Semestre não encontrado: " + ano + "/" + periodo);
    }
}
