package br.pucminas.matriculas.ui;

import br.pucminas.matriculas.Aluno;
import br.pucminas.matriculas.Curso;
import br.pucminas.matriculas.Disciplina;
import br.pucminas.matriculas.Matricula;
import br.pucminas.matriculas.Professor;
import br.pucminas.matriculas.Semestre;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CancelamentoMatriculaTest {

    @Test
    void codigo002CancelaEs002ENaoASegundaLinha() {
        List<Matricula> ativas = matriculas("ES002", "ES003", "ES004", "ES006");

        assertEquals("ES002", Main.localizarMatricula(ativas, "002").getDisciplina().getCodigo());
        assertEquals("ES002", Main.localizarMatricula(ativas, "ES002").getDisciplina().getCodigo());
        assertEquals("ES003", Main.localizarMatricula(ativas, "2").getDisciplina().getCodigo());
        assertNull(Main.localizarMatricula(ativas, "009"));
    }

    @Test
    void codigo002EscolheADisciplinaENaoASegundaLinha() {
        Curso curso = new Curso("Engenharia de Software", 240);
        Professor professor = new Professor("p1", "Ana", "ana", "senha123");
        List<Disciplina> disciplinas = List.of(
                new Disciplina("ES002", "Banco de Dados", curso, professor),
                new Disciplina("ES003", "Requisitos", curso, professor),
                new Disciplina("ES004", "Teste", curso, professor));

        assertEquals("ES002", Main.localizarDisciplina(disciplinas, "002").getCodigo());
        assertEquals("ES002", Main.localizarDisciplina(disciplinas, "ES002").getCodigo());
        assertEquals("ES003", Main.localizarDisciplina(disciplinas, "2").getCodigo());
    }

    @Test
    void dadosExistenteForaDaPastaAtualContinuaSendoUsado(@TempDir Path raiz) throws Exception {
        Path projeto = raiz.resolve("Sistema de Matriculas");
        Path pastaAtual = projeto;
        Path dadosNaRaiz = raiz.resolve("dados");
        Files.createDirectories(dadosNaRaiz);
        Files.writeString(dadosNaRaiz.resolve("secretaria.txt"), "s1|Maria|secretaria|senha123");

        Path escolhido = Main.resolverDiretorioDados(pastaAtual, projeto);
        assertEquals(dadosNaRaiz.toAbsolutePath().normalize(), escolhido);
    }

    private List<Matricula> matriculas(String... codigos) {
        Curso curso = new Curso("Engenharia de Software", 240);
        Professor professor = new Professor("p1", "Ana", "ana", "senha123");
        Aluno aluno = new Aluno("a1", "Joao", "joao", "senha123", "2026001", curso);
        Semestre semestre = new Semestre(2026, 2);
        List<Matricula> matriculas = new java.util.ArrayList<>();
        for (String codigo : codigos) {
            Disciplina disciplina = new Disciplina(codigo, codigo, curso, professor);
            matriculas.add(new Matricula(aluno, disciplina, semestre, Matricula.TIPO_OBRIGATORIA));
        }
        return matriculas;
    }
}
