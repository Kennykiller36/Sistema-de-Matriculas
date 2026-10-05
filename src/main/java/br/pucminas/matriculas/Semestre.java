package br.pucminas.matriculas;

import java.util.LinkedHashMap;
import java.util.Map;

public class Semestre {

    private int ano;
    private int periodo;
    private Curriculo curriculo;
    private PeriodoMatricula periodoMatricula;
    private final Map<String, String> situacaoOferta = new LinkedHashMap<>();
    private final Map<String, Boolean> inscricoesEncerradasOferta = new LinkedHashMap<>();

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

    /**
     * Situação desta disciplina neste semestre. Sem registro, usa a situação da disciplina.
     */
    public String situacaoNaOferta(Disciplina disciplina) {
        String situacao = situacaoOferta.get(disciplina.getCodigo());
        return situacao != null ? situacao : disciplina.getSituacao();
    }

    public String getSituacaoOferta(String codigo) {
        return situacaoOferta.get(codigo);
    }

    public Boolean getInscricoesEncerradasOferta(String codigo) {
        return inscricoesEncerradasOferta.get(codigo);
    }

    public boolean inscricoesEncerradasNaOferta(Disciplina disciplina) {
        Boolean encerradas = inscricoesEncerradasOferta.get(disciplina.getCodigo());
        return encerradas != null ? encerradas : disciplina.isInscricoesEncerradas();
    }

    public void definirOferta(String codigo, String situacao, boolean inscricoesEncerradas) {
        situacaoOferta.put(codigo, situacao);
        inscricoesEncerradasOferta.put(codigo, inscricoesEncerradas);
    }

    /**
     * Atualiza só o fechamento das inscrições, sem copiar a situação de outro semestre.
     */
    public void registrarInscricoesEncerradas(Disciplina disciplina, boolean encerradas) {
        situacaoOferta.putIfAbsent(disciplina.getCodigo(), situacaoNaOferta(disciplina));
        inscricoesEncerradasOferta.put(disciplina.getCodigo(), encerradas);
    }
}
