package br.pucminas.matriculas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Massa inicial para o primeiro acesso ao protótipo.
 */
public final class DadosIniciais {

    private DadosIniciais() {
    }

    public static void popular(Secretaria secretaria, SistemaCobranca cobranca) {
        Curso curso = secretaria.adicionarCurso(new Curso("Engenharia de Software", 240));

        Professor ana = new Professor("p1", "Ana Souza", "ana", "senha123");
        Professor carlos = new Professor("p2", "Carlos Lima", "carlos", "senha123");
        secretaria.adicionarProfessor(ana);
        secretaria.adicionarProfessor(carlos);

        secretaria.adicionarAluno(criarAluno("a1", "Joao Silva", "joao", "2026001", curso, cobranca));
        secretaria.adicionarAluno(criarAluno("a2", "Maria Costa", "maria", "2026002", curso, cobranca));
        secretaria.adicionarAluno(criarAluno("a3", "Pedro Alves", "pedro", "2026003", curso, cobranca));

        List<Disciplina> ofertadas = new ArrayList<>();
        ofertadas.add(criarDisciplina(secretaria, "ES001", "Projeto de Software", curso, ana));
        ofertadas.add(criarDisciplina(secretaria, "ES002", "Banco de Dados", curso, carlos));
        ofertadas.add(criarDisciplina(secretaria, "ES003", "Engenharia de Requisitos", curso, ana));
        ofertadas.add(criarDisciplina(secretaria, "ES004", "Teste de Software", curso, carlos));
        ofertadas.add(criarDisciplina(secretaria, "ES005", "Interacao Humano-Computador", curso, ana));
        ofertadas.add(criarDisciplina(secretaria, "ES006", "Empreendedorismo", curso, carlos));

        Semestre semestre = secretaria.abrirSemestre(2026, 2);
        secretaria.gerarCurriculo(semestre, ofertadas);
        secretaria.definirPeriodoMatriculas(
                semestre, LocalDate.now().minusDays(1), LocalDate.now().plusDays(60));
    }

    private static Aluno criarAluno(
            String id, String nome, String login, String matricula, Curso curso, SistemaCobranca cobranca) {
        Aluno aluno = new Aluno(id, nome, login, "senha123", matricula, curso);
        aluno.setSistemaCobranca(cobranca);
        return aluno;
    }

    private static Disciplina criarDisciplina(
            Secretaria secretaria, String codigo, String nome, Curso curso, Professor professor) {
        Disciplina disciplina = new Disciplina(codigo, nome, curso, professor);
        secretaria.cadastrarDisciplina(disciplina);
        return disciplina;
    }
}
