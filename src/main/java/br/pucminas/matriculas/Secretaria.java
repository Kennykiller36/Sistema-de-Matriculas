package br.pucminas.matriculas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Secretaria extends Usuario {

    private final List<Curso> cursos = new ArrayList<>();
    private final List<Disciplina> disciplinas = new ArrayList<>();
    private final List<Aluno> alunos = new ArrayList<>();
    private final List<Professor> professores = new ArrayList<>();
    private final List<Semestre> semestres = new ArrayList<>();
    private PeriodoMatricula periodoMatriculas;

    public Secretaria(String id, String nome, String login, String senha) {
        super(id, nome, login, senha);
    }

    /**
     * HU09 — lista as disciplinas cadastradas.
     */
    public List<Disciplina> consultarDisciplinas() {
        return disciplinas;
    }

    /**
     * HU10 — inclui consultar disciplinas.
     */
    public void cadastrarDisciplina(Disciplina disciplina) {
    }

    /**
     * HU12 — a disciplina deixa de aparecer nas consultas e nos curriculos futuros.
     * Inclui consultar disciplinas.
     */
    public void excluirDisciplina(Disciplina disciplina) {
    }

    /**
     * HU11 — seleciona o semestre e as disciplinas ofertadas. Inclui consultar disciplinas.
     */
    public Curriculo gerarCurriculo(Semestre semestre, List<Disciplina> disciplinas) {
        return null;
    }

    /**
     * HU13 — define inicio e fim do periodo em que o aluno pode matricular ou cancelar.
     */
    public PeriodoMatricula definirPeriodoMatriculas(Semestre semestre, LocalDate inicio, LocalDate fim) {
        return null;
    }

    /**
     * HU13 — encerra o periodo e avalia cada disciplina (ativa com 3 ou mais alunos).
     */
    public void encerrarPeriodoMatriculas(PeriodoMatricula periodo) {
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public List<Disciplina> getDisciplinas() {
        return disciplinas;
    }

    public List<Aluno> getAlunos() {
        return alunos;
    }

    public List<Professor> getProfessores() {
        return professores;
    }

    public List<Semestre> getSemestres() {
        return semestres;
    }

    public PeriodoMatricula getPeriodoMatriculas() {
        return periodoMatriculas;
    }

    public void setPeriodoMatriculas(PeriodoMatricula periodoMatriculas) {
        this.periodoMatriculas = periodoMatriculas;
    }
}
