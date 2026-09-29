package br.pucminas.matriculas;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegrasMatriculaTest {

    @Test
    void loginSoLiberaComSenhaCorreta() {
        Aluno aluno = new Aluno("a1", "Joao", "joao", "senha123", "2026001", new Curso("ES", 240));

        assertTrue(aluno.autenticar("joao", "senha123"));
        assertTrue(aluno.autenticar("Joao", "senha123"));
        assertFalse(aluno.autenticar("joao", "errada"));
        assertFalse(aluno.autenticar("outro", "senha123"));
    }

    @Test
    void limitaQuatroObrigatoriasEDuasOptativas() {
        Cenario cenario = cenarioComDisciplinas(7);
        Aluno aluno = novoAluno(cenario, "joao", "1");

        for (int i = 0; i < 4; i++) {
            aluno.matricularObrigatoria(cenario.disciplinas.get(i), cenario.semestre);
        }
        assertThrows(RegraNegocioException.class,
                () -> aluno.matricularObrigatoria(cenario.disciplinas.get(4), cenario.semestre));

        aluno.matricularOptativa(cenario.disciplinas.get(4), cenario.semestre);
        aluno.matricularOptativa(cenario.disciplinas.get(5), cenario.semestre);
        assertThrows(RegraNegocioException.class,
                () -> aluno.matricularOptativa(cenario.disciplinas.get(6), cenario.semestre));
    }

    @Test
    void impedeMatriculaDuplicadaForaDoPeriodoEForaDoCurriculo() {
        Cenario cenario = cenarioComDisciplinas(2);
        Aluno aluno = novoAluno(cenario, "joao", "1");
        aluno.matricularObrigatoria(cenario.disciplinas.get(0), cenario.semestre);

        assertThrows(RegraNegocioException.class,
                () -> aluno.matricularOptativa(cenario.disciplinas.get(0), cenario.semestre));

        Disciplina fora = new Disciplina("XX", "Fora", cenario.curso, cenario.professor);
        cenario.secretaria.cadastrarDisciplina(fora);
        assertThrows(RegraNegocioException.class,
                () -> aluno.matricularObrigatoria(fora, cenario.semestre));

        cenario.semestre.getPeriodoMatricula().encerrar();
        assertThrows(RegraNegocioException.class,
                () -> aluno.matricularObrigatoria(cenario.disciplinas.get(1), cenario.semestre));
    }

    @Test
    void encerraInscricoesEm60EReabreVagaNoCancelamento() {
        Cenario cenario = cenarioComDisciplinas(1);
        List<Aluno> alunos = new ArrayList<>();
        for (int i = 0; i < Disciplina.CAPACIDADE_PADRAO; i++) {
            Aluno aluno = novoAluno(cenario, "aluno" + i, "m" + i);
            aluno.matricularObrigatoria(cenario.disciplinas.get(0), cenario.semestre);
            alunos.add(aluno);
        }

        Disciplina disciplina = cenario.disciplinas.get(0);
        assertTrue(disciplina.isInscricoesEncerradas());
        assertFalse(disciplina.possuiVaga());
        assertEquals(0, disciplina.getVagas());

        Aluno extra = novoAluno(cenario, "extra", "extra");
        assertThrows(RegraNegocioException.class,
                () -> extra.matricularObrigatoria(disciplina, cenario.semestre));

        alunos.get(0).cancelarMatricula(alunos.get(0).getMatriculas().get(0));
        assertTrue(disciplina.possuiVaga());
        assertFalse(disciplina.isInscricoesEncerradas());
        assertEquals(0, cenario.professor.listarAlunos(disciplina).stream()
                .filter(aluno -> aluno.getLogin().equals("aluno0"))
                .count());

        extra.matricularObrigatoria(disciplina, cenario.semestre);
        assertEquals(Disciplina.CAPACIDADE_PADRAO, disciplina.obterQuantidadeInscritos());
        assertEquals(Disciplina.CAPACIDADE_PADRAO, cenario.professor.listarAlunos(disciplina).size());
    }

    @Test
    void aoEncerrarPeriodoAtivaTurmaCom3ECancelaComMenos() {
        Cenario cenario = cenarioComDisciplinas(2);
        Aluno joao = novoAluno(cenario, "joao", "1");
        Aluno maria = novoAluno(cenario, "maria", "2");
        Aluno pedro = novoAluno(cenario, "pedro", "3");

        joao.matricularObrigatoria(cenario.disciplinas.get(0), cenario.semestre);
        maria.matricularObrigatoria(cenario.disciplinas.get(0), cenario.semestre);
        pedro.matricularObrigatoria(cenario.disciplinas.get(0), cenario.semestre);
        joao.matricularOptativa(cenario.disciplinas.get(1), cenario.semestre);
        maria.matricularOptativa(cenario.disciplinas.get(1), cenario.semestre);

        cenario.secretaria.encerrarPeriodoMatriculas(cenario.semestre.getPeriodoMatricula());

        assertEquals(Disciplina.ATIVA, cenario.disciplinas.get(0).getSituacao());
        assertEquals(Disciplina.CANCELADA, cenario.disciplinas.get(1).getSituacao());
        assertFalse(cenario.semestre.getPeriodoMatricula().estaAberto(LocalDate.now()));
        assertThrows(RegraNegocioException.class,
                () -> pedro.cancelarMatricula(pedro.getMatriculas().get(0)));
    }

    @Test
    void notificaCobrancaComAsDisciplinasDoSemestre() {
        Cenario cenario = cenarioComDisciplinas(2);
        CobrancaMemoria cobranca = new CobrancaMemoria();
        Aluno aluno = novoAluno(cenario, "joao", "1");
        aluno.setSistemaCobranca(cobranca);

        aluno.matricularObrigatoria(cenario.disciplinas.get(0), cenario.semestre);
        aluno.matricularOptativa(cenario.disciplinas.get(1), cenario.semestre);

        assertEquals(2, cobranca.chamadas);
        assertEquals(2, cobranca.ultima.size());
        assertEquals(aluno, cobranca.aluno);
    }

    @Test
    void exclusaoRemoveDisciplinaDosCurriculosAindaAbertos() {
        Cenario cenario = cenarioComDisciplinas(1);
        Disciplina disciplina = cenario.disciplinas.get(0);

        Semestre passado = cenario.secretaria.abrirSemestre(2025, 2);
        cenario.secretaria.gerarCurriculo(passado, List.of(disciplina));
        PeriodoMatricula periodoPassado = cenario.secretaria.definirPeriodoMatriculas(
                passado, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1));
        periodoPassado.encerrar();

        cenario.secretaria.excluirDisciplina(disciplina);

        assertFalse(cenario.secretaria.consultarDisciplinas().contains(disciplina));
        assertFalse(cenario.semestre.getCurriculo().getDisciplinas().contains(disciplina));
        assertTrue(passado.getCurriculo().getDisciplinas().contains(disciplina));
    }

    private Cenario cenarioComDisciplinas(int quantidade) {
        Secretaria secretaria = new Secretaria("s1", "Maria", "secretaria", "senha123");
        Curso curso = secretaria.adicionarCurso(new Curso("Engenharia de Software", 240));
        Professor professor = new Professor("p1", "Ana", "ana", "senha123");
        secretaria.adicionarProfessor(professor);

        List<Disciplina> disciplinas = new ArrayList<>();
        for (int i = 1; i <= quantidade; i++) {
            Disciplina disciplina = new Disciplina("D" + i, "Disciplina " + i, curso, professor);
            secretaria.cadastrarDisciplina(disciplina);
            disciplinas.add(disciplina);
        }
        Semestre semestre = secretaria.abrirSemestre(2026, 2);
        secretaria.gerarCurriculo(semestre, disciplinas);
        secretaria.definirPeriodoMatriculas(
                semestre, LocalDate.now().minusDays(1), LocalDate.now().plusDays(10));
        return new Cenario(secretaria, curso, professor, semestre, disciplinas);
    }

    private Aluno novoAluno(Cenario cenario, String login, String matricula) {
        Aluno aluno = new Aluno("id-" + login, login, login, "senha123", matricula, cenario.curso);
        cenario.secretaria.adicionarAluno(aluno);
        return aluno;
    }

    private static final class Cenario {
        private final Secretaria secretaria;
        private final Curso curso;
        private final Professor professor;
        private final Semestre semestre;
        private final List<Disciplina> disciplinas;

        private Cenario(
                Secretaria secretaria,
                Curso curso,
                Professor professor,
                Semestre semestre,
                List<Disciplina> disciplinas) {
            this.secretaria = secretaria;
            this.curso = curso;
            this.professor = professor;
            this.semestre = semestre;
            this.disciplinas = disciplinas;
        }
    }

    private static final class CobrancaMemoria implements SistemaCobranca {
        private int chamadas;
        private Aluno aluno;
        private List<Disciplina> ultima = List.of();

        @Override
        public void cobrar(Aluno aluno, Semestre semestre, List<Disciplina> disciplinas) {
            chamadas++;
            this.aluno = aluno;
            this.ultima = disciplinas;
        }
    }
}
