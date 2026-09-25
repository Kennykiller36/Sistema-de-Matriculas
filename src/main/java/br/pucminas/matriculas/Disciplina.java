package br.pucminas.matriculas;

import java.util.ArrayList;
import java.util.List;

public class Disciplina {

    private String codigo;
    private String nome;
    private int capacidadeMaxima = 60;
    private int minimoAlunos = 3;
    private boolean inscricoesEncerradas;
    private String situacao = "EM_INSCRICAO";
    private Curso curso;
    private Professor professor;
    private final List<Matricula> matriculas = new ArrayList<>();

    public Disciplina(String codigo, String nome, Curso curso, Professor professor) {
        this.codigo = codigo;
        this.nome = nome;
        this.curso = curso;
        this.professor = professor;
    }

    public int obterQuantidadeInscritos() {
        return 0;
    }

    public boolean possuiVaga() {
        return false;
    }

    /**
     * Encerra inscricoes quando a disciplina atinge 60 alunos.
     */
    public void encerrarInscricoes() {
    }

    /**
     * Ao fim do periodo: pelo menos 3 alunos deixa a disciplina ATIVA;
     * menos de 3, CANCELADA.
     */
    public void avaliarAoFimDoPeriodo() {
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public void setCapacidadeMaxima(int capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public int getMinimoAlunos() {
        return minimoAlunos;
    }

    public void setMinimoAlunos(int minimoAlunos) {
        this.minimoAlunos = minimoAlunos;
    }

    public boolean isInscricoesEncerradas() {
        return inscricoesEncerradas;
    }

    public void setInscricoesEncerradas(boolean inscricoesEncerradas) {
        this.inscricoesEncerradas = inscricoesEncerradas;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public List<Matricula> getMatriculas() {
        return matriculas;
    }
}
