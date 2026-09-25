package br.pucminas.matriculas;

public class Matricula {

    private Aluno aluno;
    private Disciplina disciplina;
    private Semestre semestre;
    private String tipo;
    private String situacao;

    public Matricula(Aluno aluno, Disciplina disciplina, Semestre semestre, String tipo) {
        this.aluno = aluno;
        this.disciplina = disciplina;
        this.semestre = semestre;
        this.tipo = tipo;
        this.situacao = "ATIVA";
    }

    /**
     * HU04 — cancela a matricula e devolve a vaga da disciplina.
     */
    public void cancelar() {
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    public Semestre getSemestre() {
        return semestre;
    }

    public void setSemestre(Semestre semestre) {
        this.semestre = semestre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }
}
