package br.pucminas.matriculas;

public class Semestre {

    private int ano;
    private int periodo;
    private Curriculo curriculo;
    private PeriodoMatricula periodoMatricula;

    public Semestre(int ano, int periodo) {
        this.ano = ano;
        this.periodo = periodo;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public int getPeriodo() {
        return periodo;
    }

    public void setPeriodo(int periodo) {
        this.periodo = periodo;
    }

    public Curriculo getCurriculo() {
        return curriculo;
    }

    public void setCurriculo(Curriculo curriculo) {
        this.curriculo = curriculo;
    }

    public PeriodoMatricula getPeriodoMatricula() {
        return periodoMatricula;
    }

    public void setPeriodoMatricula(PeriodoMatricula periodoMatricula) {
        this.periodoMatricula = periodoMatricula;
    }
}
