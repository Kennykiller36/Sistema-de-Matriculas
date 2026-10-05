package br.pucminas.matriculas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SondagemBugTest {

    @TempDir
    Path diretorio;

    @Test
    void lotacaoDeUmSemestreNaoFechaOOutro() {
        Secretaria secretaria = secretariaComDisciplina();
        Disciplina disciplina = secretaria.consultarDisciplinas().get(0);
        disciplina.setCapacidadeMaxima(1);
        Semestre primeiro = secretaria.getSemestres().get(0);
        aluno(secretaria, "joao", "1").matricularObrigatoria(disciplina, primeiro);
        assertTrue(disciplina.isInscricoesEncerradas());

        Semestre seguinte = secretaria.abrirSemestre(2027, 1);
        secretaria.gerarCurriculo(seguinte, List.of(disciplina));
        secretaria.definirPeriodoMatriculas(
                seguinte, LocalDate.now().minusDays(1), LocalDate.now().plusDays(15));

        assertFalse(seguinte.inscricoesEncerradasNaOferta(disciplina));
        assertEquals(Disciplina.EM_INSCRICAO, seguinte.situacaoNaOferta(disciplina));
        aluno(secretaria, "maria", "2").matricularObrigatoria(disciplina, seguinte);
        assertEquals(1, disciplina.obterQuantidadeInscritos(seguinte));
    }

    @Test
    void consultaMostraSemestreAbertoQuandoOutroFoiCancelado() {
        Secretaria secretaria = secretariaComDisciplina();
        Disciplina disciplina = secretaria.consultarDisciplinas().get(0);
        Semestre primeiro = secretaria.getSemestres().get(0);
        secretaria.encerrarPeriodoMatriculas(primeiro.getPeriodoMatricula());
        assertEquals(Disciplina.CANCELADA, disciplina.getSituacao());
        assertTrue(disciplina.isInscricoesEncerradas());

        Semestre seguinte = secretaria.abrirSemestre(2027, 1);
        secretaria.gerarCurriculo(seguinte, List.of(disciplina));
        secretaria.definirPeriodoMatriculas(
                seguinte, LocalDate.now().minusDays(1), LocalDate.now().plusDays(15));

        assertEquals(seguinte, br.pucminas.matriculas.ui.Main.semestreEmInscricao(
                disciplina, secretaria.getSemestres()));
        assertEquals(Disciplina.EM_INSCRICAO, seguinte.situacaoNaOferta(disciplina));
        assertFalse(seguinte.inscricoesEncerradasNaOferta(disciplina));
        aluno(secretaria, "maria", "2").matricularObrigatoria(disciplina, seguinte);
    }

    @Test
    void recriarCodigoExcluidoNaoMisturaCurriculoAntigo() {
        Aplicacao aplicacao = new Aplicacao(diretorio);
        Semestre semestre = aplicacao.semestresAbertos().get(0);
        Disciplina es006 = buscar(aplicacao, "ES006");
        aplicacao.encerrarPeriodo(semestre.getPeriodoMatricula());
        aplicacao.excluirDisciplina(es006);

        Curso curso = aplicacao.getSecretaria().getCursos().get(0);
        Professor carlos = aplicacao.getSecretaria().getProfessores().get(1);
        Semestre seguinte = aplicacao.abrirSemestre(2027, 1);
        Disciplina recriada = new Disciplina("ES006", "Empreendedorismo Novo", curso, carlos);
        aplicacao.cadastrarDisciplina(recriada);
        aplicacao.gerarCurriculo(seguinte, List.of(recriada));
        aplicacao.definirPeriodo(seguinte, LocalDate.now().minusDays(1), LocalDate.now().plusDays(20));

        Aplicacao recarregada = new Aplicacao(diretorio);
        int copias = 0;
        for (Disciplina disciplina : recarregada.getSecretaria().consultarDisciplinas()) {
            if ("ES006".equals(disciplina.getCodigo())) {
                copias++;
                assertEquals("Empreendedorismo Novo", disciplina.getNome());
            }
        }
        assertEquals(1, copias);
        Semestre seguinteSalvo = null;
        Semestre anteriorSalvo = null;
        for (Semestre item : recarregada.getSecretaria().getSemestres()) {
            if (item.getAno() == 2027) {
                seguinteSalvo = item;
            }
            if (item.getAno() == 2026) {
                anteriorSalvo = item;
            }
        }
        assertEquals("Empreendedorismo Novo",
                seguinteSalvo.getCurriculo().getDisciplinas().get(0).getNome());
        boolean antigoNoSemestreEncerrado = false;
        for (Disciplina disciplina : anteriorSalvo.getCurriculo().getDisciplinas()) {
            if ("ES006".equals(disciplina.getCodigo()) && "Empreendedorismo".equals(disciplina.getNome())) {
                antigoNoSemestreEncerrado = true;
            }
        }
        assertTrue(antigoNoSemestreEncerrado);
    }

    @Test
    void codigoComVirgulaERecusado() {
        Secretaria secretaria = secretariaComDisciplina();
        Curso curso = secretaria.getCursos().get(0);
        Professor professor = secretaria.getProfessores().get(0);
        assertThrows(RegraNegocioException.class,
                () -> secretaria.cadastrarDisciplina(new Disciplina("ES,007", "Especial", curso, professor)));
    }

    private Disciplina buscar(Aplicacao aplicacao, String codigo) {
        for (Disciplina disciplina : aplicacao.getSecretaria().consultarDisciplinas()) {
            if (codigo.equals(disciplina.getCodigo())) {
                return disciplina;
            }
        }
        throw new AssertionError(codigo);
    }

    private Secretaria secretariaComDisciplina() {
        Secretaria secretaria = new Secretaria("s1", "Maria", "secretaria", "senha123");
        Curso curso = secretaria.adicionarCurso(new Curso("Engenharia de Software", 240));
        Professor professor = new Professor("p1", "Ana", "ana", "senha123");
        secretaria.adicionarProfessor(professor);
        Disciplina disciplina = new Disciplina("D1", "Disciplina 1", curso, professor);
        secretaria.cadastrarDisciplina(disciplina);
        Semestre semestre = secretaria.abrirSemestre(2026, 2);
        secretaria.gerarCurriculo(semestre, List.of(disciplina));
        secretaria.definirPeriodoMatriculas(
                semestre, LocalDate.now().minusDays(1), LocalDate.now().plusDays(10));
        return secretaria;
    }

    private Aluno aluno(Secretaria secretaria, String login, String matricula) {
        Aluno aluno = new Aluno("id-" + login, login, login, "senha123", matricula,
                secretaria.getCursos().get(0));
        secretaria.adicionarAluno(aluno);
        return aluno;
    }
}
