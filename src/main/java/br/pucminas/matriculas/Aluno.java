package br.pucminas.matriculas;

import java.util.ArrayList;
import java.util.List;

public class Aluno extends Usuario {

    public static final int MAX_OBRIGATORIAS = 4;
    public static final int MAX_OPTATIVAS = 2;

    private String matricula;
    private Curso curso;
    private SistemaCobranca sistemaCobranca;
    private final List<Matricula> matriculas = new ArrayList<>();

    public Aluno(String id, String nome, String login, String senha, String matricula, Curso curso) {
        super(id, nome, login, senha);
        this.matricula = matricula;
        this.curso = curso;
    }

    /**
     * HU02 — até 4 obrigatórias, sem duplicidade, vaga (máx. 60) e período aberto.
     */
    public Matricula matricularObrigatoria(Disciplina disciplina, Semestre semestre) {
        return matricular(disciplina, semestre, Matricula.TIPO_OBRIGATORIA, MAX_OBRIGATORIAS);
    }

    /**
     * HU03 — até 2 optativas, sem duplicidade, vaga (máx. 60) e período aberto.
     */
    public Matricula matricularOptativa(Disciplina disciplina, Semestre semestre) {
        return matricular(disciplina, semestre, Matricula.TIPO_OPTATIVA, MAX_OPTATIVAS);
    }

    /**
     * HU04 — só no período aberto. A vaga volta para a disciplina.
     */
    public void cancelarMatricula(Matricula matriculaAlvo) {
        if (matriculaAlvo == null || matriculaAlvo.getAluno() != this || !matriculas.contains(matriculaAlvo)) {
            throw new RegraNegocioException("Matrícula não encontrada.");
        }
        Semestre semestre = matriculaAlvo.getSemestre();
        if (!periodoAberto(semestre)) {
            throw new RegraNegocioException("Cancelamento só é permitido no período de matrículas.");
        }
        if (!matriculaAlvo.isAtiva()) {
            throw new RegraNegocioException("Matrícula já cancelada.");
        }
        matriculaAlvo.cancelar();
        Disciplina disciplina = matriculaAlvo.getDisciplina();
        if (!Disciplina.CANCELADA.equals(situacaoNaOferta(disciplina, semestre))
                && disciplina.obterQuantidadeInscritos(semestre) < disciplina.getCapacidadeMaxima()) {
            semestre.registrarInscricoesEncerradas(disciplina, false);
            if (disciplina.obterQuantidadeInscritos() < disciplina.getCapacidadeMaxima()) {
                disciplina.setInscricoesEncerradas(false);
            }
        }
        finalizarMatricula(semestre);
    }

    /**
     * HU06 — confirma as disciplinas do semestre e notifica a cobrança (HU07).
     */
    public void finalizarMatricula(Semestre semestre) {
        if (semestre == null) {
            throw new RegraNegocioException("Informe o semestre.");
        }
        if (sistemaCobranca == null) {
            return;
        }
        List<Disciplina> confirmadas = new ArrayList<>();
        for (Matricula matriculaAtual : matriculas) {
            if (matriculaAtual.isAtiva() && mesmoSemestre(matriculaAtual.getSemestre(), semestre)) {
                confirmadas.add(matriculaAtual.getDisciplina());
            }
        }
        sistemaCobranca.cobrar(this, semestre, confirmadas);
    }

    public void setSistemaCobranca(SistemaCobranca sistemaCobranca) {
        this.sistemaCobranca = sistemaCobranca;
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

    private Matricula matricular(Disciplina disciplina, Semestre semestre, String tipo, int limite) {
        if (disciplina == null || semestre == null) {
            throw new RegraNegocioException("Informe a disciplina e o semestre.");
        }
        if (!periodoAberto(semestre)) {
            throw new RegraNegocioException("Fora do período de matrículas.");
        }
        Curriculo curriculo = semestre.getCurriculo();
        if (curriculo == null || !curriculo.getDisciplinas().contains(disciplina)) {
            throw new RegraNegocioException("Disciplina não ofertada no currículo deste semestre.");
        }
        if (curso == null || disciplina.getCurso() == null
                || !curso.getNome().equals(disciplina.getCurso().getNome())) {
            throw new RegraNegocioException("A disciplina não pertence ao curso do aluno.");
        }
        if (Disciplina.CANCELADA.equals(situacaoNaOferta(disciplina, semestre))) {
            throw new RegraNegocioException("Disciplina cancelada.");
        }
        if (!possuiVagaNaOferta(disciplina, semestre)) {
            throw new RegraNegocioException(
                    "Não há vaga nesta disciplina (limite de " + disciplina.getCapacidadeMaxima() + " alunos).");
        }
        if (jaMatriculado(disciplina, semestre)) {
            throw new RegraNegocioException("Você já está matriculado nesta disciplina.");
        }
        if (contarAtivas(semestre, tipo) >= limite) {
            String nomeTipo = Matricula.TIPO_OBRIGATORIA.equals(tipo) ? "obrigatórias" : "optativas";
            throw new RegraNegocioException("Limite de " + limite + " disciplinas " + nomeTipo + " atingido.");
        }

        Matricula nova = new Matricula(this, disciplina, semestre, tipo);
        matriculas.add(nova);
        disciplina.getMatriculas().add(nova);
        if (disciplina.obterQuantidadeInscritos(semestre) >= disciplina.getCapacidadeMaxima()) {
            disciplina.encerrarInscricoes();
            semestre.registrarInscricoesEncerradas(disciplina, true);
        }
        inscreverAoSemestre(semestre);
        return nova;
    }

    /**
     * HU05 — inclui finalizar a matrícula (HU06), que notifica a cobrança (HU07).
     */
    private void inscreverAoSemestre(Semestre semestre) {
        finalizarMatricula(semestre);
    }

    private boolean periodoAberto(Semestre semestre) {
        PeriodoMatricula periodo = semestre.getPeriodoMatricula();
        return periodo != null && periodo.estaAberto(java.time.LocalDate.now());
    }

    private boolean jaMatriculado(Disciplina disciplina, Semestre semestre) {
        for (Matricula matriculaAtual : matriculas) {
            if (matriculaAtual.isAtiva()
                    && matriculaAtual.getDisciplina().getCodigo().equals(disciplina.getCodigo())
                    && mesmoSemestre(matriculaAtual.getSemestre(), semestre)) {
                return true;
            }
        }
        return false;
    }

    private int contarAtivas(Semestre semestre, String tipo) {
        int total = 0;
        for (Matricula matriculaAtual : matriculas) {
            if (matriculaAtual.isAtiva()
                    && tipo.equals(matriculaAtual.getTipo())
                    && mesmoSemestre(matriculaAtual.getSemestre(), semestre)) {
                total++;
            }
        }
        return total;
    }

    private boolean mesmoSemestre(Semestre primeiro, Semestre segundo) {
        return primeiro.getAno() == segundo.getAno() && primeiro.getPeriodo() == segundo.getPeriodo();
    }

    private String situacaoNaOferta(Disciplina disciplina, Semestre semestre) {
        return semestre.situacaoNaOferta(disciplina);
    }

    private boolean possuiVagaNaOferta(Disciplina disciplina, Semestre semestre) {
        if (Disciplina.CANCELADA.equals(situacaoNaOferta(disciplina, semestre))) {
            return false;
        }
        return !semestre.inscricoesEncerradasNaOferta(disciplina)
                && disciplina.obterQuantidadeInscritos(semestre) < disciplina.getCapacidadeMaxima();
    }
}
