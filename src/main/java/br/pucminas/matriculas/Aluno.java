package br.pucminas.matriculas;

import java.util.ArrayList;
import java.util.List;

public class Aluno extends Usuario {

    public static final int MAX_OBRIGATORIAS = 4;
    public static final int MAX_OPTATIVAS = 2;

    private String matricula;
    private Curso curso;
    private final List<Matricula> matriculas = new ArrayList<>();

    public Aluno(String id, String nome, String login, String senha, String matricula, Curso curso) {
        super(id, nome, login, senha);
        this.matricula = matricula;
        this.curso = curso;
    }

    /**
     * HU02 — ate 4 obrigatorias, sem duplicidade, vaga (max. 60) e periodo aberto.
     */
    public Matricula matricularObrigatoria(Disciplina disciplina, Semestre semestre) {
        return null;
    }

    /**
     * HU03 — ate 2 optativas, sem duplicidade, vaga (max. 60) e periodo aberto.
     */
    public Matricula matricularOptativa(Disciplina disciplina, Semestre semestre) {
        return null;
    }

    /**
     * HU04 — so no periodo aberto. A vaga volta para a disciplina.
     */
    public void cancelarMatricula(Matricula matricula) {
    }

    /**
     * HU06 — confirma as disciplinas do semestre e notifica a cobranca.
     */
    public void finalizarMatricula(Semestre semestre) {
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public List<Matricula> getMatriculas() {
        return matriculas;
    }
}
