package br.pucminas.matriculas;

import java.util.ArrayList;
import java.util.List;

public class Disciplina {

    public static final int CAPACIDADE_PADRAO = 60;
    public static final int MINIMO_PARA_ATIVAR = 3;
    public static final String EM_INSCRICAO = "EM_INSCRICAO";
    public static final String ATIVA = "ATIVA";
    public static final String CANCELADA = "CANCELADA";

    private String codigo;
    private String nome;
    private int capacidadeMaxima = CAPACIDADE_PADRAO;
    private int minimoAlunos = MINIMO_PARA_ATIVAR;
    private boolean inscricoesEncerradas;
    private String situacao = EM_INSCRICAO;
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
        int quantidade = 0;
        for (Matricula matricula : matriculas) {
            if (matricula.isAtiva()) {
                quantidade++;
            }
        }
        return quantidade;
    }

    /**
     * Alunos com matrícula ativa nesta disciplina apenas no semestre informado.
     */
    public int obterQuantidadeInscritos(Semestre semestre) {
        if (semestre == null) {
            return obterQuantidadeInscritos();
        }
        int quantidade = 0;
        for (Matricula matricula : matriculas) {
            if (matricula.isAtiva() && matricula.getSemestre() != null
                    && matricula.getSemestre().getAno() == semestre.getAno()
                    && matricula.getSemestre().getPeriodo() == semestre.getPeriodo()) {
                quantidade++;
            }
        }
        return quantidade;
    }

    /**
     * Vagas restantes. Corresponde ao atributo {@code vagas} do diagrama de classes.
     */
    public int getVagas() {
        return Math.max(0, capacidadeMaxima - obterQuantidadeInscritos());
    }

    public boolean possuiVaga() {
        return !inscricoesEncerradas
                && !CANCELADA.equals(situacao)
                && obterQuantidadeInscritos() < capacidadeMaxima;
    }

    /**
     * Encerra inscrições quando a disciplina atinge 60 alunos.
     */
    public void encerrarInscricoes() {
        this.inscricoesEncerradas = true;
    }

    /**
     * Ao fim do período: pelo menos 3 alunos deixa a disciplina ATIVA;
     * menos de 3, CANCELADA.
     */
    public void avaliarAoFimDoPeriodo() {
        avaliarAoFimDoPeriodo(null);
    }

    /**
     * Avalia a oferta deste semestre. Outro semestre que reutilize a disciplina começa de novo.
     */
    public void avaliarAoFimDoPeriodo(Semestre semestre) {
        int inscritos = obterQuantidadeInscritos(semestre);
        if (inscritos >= minimoAlunos) {
            this.situacao = ATIVA;
            if (semestre != null) {
                semestre.definirOferta(codigo, ATIVA, inscricoesEncerradas);
            }
        } else {
            this.situacao = CANCELADA;
            this.inscricoesEncerradas = true;
            if (semestre != null) {
                semestre.definirOferta(codigo, CANCELADA, true);
            }
        }
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
