package br.pucminas.matriculas;

import java.time.LocalDate;

public class PeriodoMatricula {

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private boolean encerrado;
    private Semestre semestre;

    public PeriodoMatricula(Semestre semestre, LocalDate dataInicio, LocalDate dataFim) {
        this.semestre = semestre;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public boolean estaAberto(LocalDate data) {
        return false;
    }

    public void encerrar() {
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public boolean isEncerrado() {
        return encerrado;
    }

    public void setEncerrado(boolean encerrado) {
        this.encerrado = encerrado;
    }

    public Semestre getSemestre() {
        return semestre;
    }

    public void setSemestre(Semestre semestre) {
        this.semestre = semestre;
    }
}
