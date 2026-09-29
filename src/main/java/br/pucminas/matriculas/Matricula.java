package br.pucminas.matriculas;

public class Matricula {

    public static final String TIPO_OBRIGATORIA = "OBRIGATORIA";
    public static final String TIPO_OPTATIVA = "OPTATIVA";
    public static final String SITUACAO_ATIVA = "ATIVA";
    public static final String SITUACAO_CANCELADA = "CANCELADA";

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
        this.situacao = SITUACAO_ATIVA;
    }

    /**
     * HU04 — cancela a matrícula e devolve a vaga da disciplina.
     */
    public void cancelar() {
        this.situacao = SITUACAO_CANCELADA;
    }

    public boolean isAtiva() {
        return SITUACAO_ATIVA.equals(situacao);
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
