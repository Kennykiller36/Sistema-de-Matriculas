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
        consultarDisciplinas();
        if (disciplina == null || textoInvalido(disciplina.getCodigo()) || textoInvalido(disciplina.getNome())) {
            throw new RegraNegocioException("Informe código e nome da disciplina.");
        }
        if (disciplina.getCodigo().contains("|") || disciplina.getCodigo().contains(",")
                || disciplina.getCodigo().contains("@") || disciplina.getNome().contains("|")) {
            throw new RegraNegocioException("Código não pode conter '|', ',' ou '@'.");
        }
        for (Disciplina existente : disciplinas) {
            if (existente.getCodigo().equalsIgnoreCase(disciplina.getCodigo())) {
                throw new RegraNegocioException("Já existe disciplina com este código.");
            }
        }
        if (disciplina.getCurso() == null || !cursos.contains(disciplina.getCurso())) {
            throw new RegraNegocioException("A disciplina deve pertencer a um curso cadastrado.");
        }
        if (disciplina.getProfessor() != null && !professores.contains(disciplina.getProfessor())) {
            throw new RegraNegocioException("Professor não cadastrado.");
        }
        disciplinas.add(disciplina);
        disciplina.getCurso().adicionarDisciplina(disciplina);
        if (disciplina.getProfessor() != null
                && !disciplina.getProfessor().getDisciplinas().contains(disciplina)) {
            disciplina.getProfessor().getDisciplinas().add(disciplina);
        }
    }

    /**
     * HU12 — a disciplina deixa de aparecer nas consultas e nos currículos futuros.
     * Inclui consultar disciplinas.
     */
    public void excluirDisciplina(Disciplina disciplina) {
        consultarDisciplinas();
        if (disciplina == null || !disciplinas.contains(disciplina)) {
            throw new RegraNegocioException("Disciplina não encontrada.");
        }
        if (disciplina.obterQuantidadeInscritos() > 0) {
            throw new RegraNegocioException("Não é possível excluir disciplina com alunos matriculados.");
        }
        disciplinas.remove(disciplina);
        if (disciplina.getCurso() != null) {
            disciplina.getCurso().getDisciplinas().remove(disciplina);
        }
        if (disciplina.getProfessor() != null && !permaneceEmSemestreEncerrado(disciplina)) {
            disciplina.getProfessor().getDisciplinas().remove(disciplina);
        }
        for (Semestre semestre : semestres) {
            PeriodoMatricula periodo = semestre.getPeriodoMatricula();
            boolean futuro = periodo == null || !periodo.isEncerrado();
            if (futuro && semestre.getCurriculo() != null) {
                semestre.getCurriculo().getDisciplinas().remove(disciplina);
            }
        }
    }

    /**
     * HU11 — seleciona o semestre e as disciplinas ofertadas. Inclui consultar disciplinas.
     */
    public Curriculo gerarCurriculo(Semestre semestre, List<Disciplina> selecionadas) {
        consultarDisciplinas();
        if (semestre == null) {
            throw new RegraNegocioException("Informe o semestre.");
        }
        if (selecionadas == null || selecionadas.isEmpty()) {
            throw new RegraNegocioException("Selecione ao menos uma disciplina.");
        }
        if (semestre.getCurriculo() != null) {
            for (Disciplina antiga : semestre.getCurriculo().getDisciplinas()) {
                if (!selecionadas.contains(antiga) && antiga.obterQuantidadeInscritos(semestre) > 0) {
                    throw new RegraNegocioException(
                            "Não é possível remover do currículo a disciplina "
                                    + antiga.getCodigo() + " com alunos matriculados.");
                }
            }
        }
        if (!semestres.contains(semestre)) {
            semestres.add(semestre);
        }
        Curriculo curriculo = new Curriculo(semestre);
        for (Disciplina disciplina : selecionadas) {
            if (!disciplinas.contains(disciplina)) {
                throw new RegraNegocioException("Disciplina não cadastrada: " + disciplina.getCodigo());
            }
            curriculo.adicionarDisciplina(disciplina);
            if (semestre.getSituacaoOferta(disciplina.getCodigo()) == null) {
                semestre.definirOferta(disciplina.getCodigo(), Disciplina.EM_INSCRICAO, false);
            }
        }
        semestre.setCurriculo(curriculo);
        return curriculo;
    }

    /**
     * HU13 — define início e fim do período em que o aluno pode matricular ou cancelar.
     */
    public PeriodoMatricula definirPeriodoMatriculas(Semestre semestre, LocalDate inicio, LocalDate fim) {
        if (semestre == null || inicio == null || fim == null) {
            throw new RegraNegocioException("Informe semestre e datas.");
        }
        if (fim.isBefore(inicio)) {
            throw new RegraNegocioException("A data fim deve ser igual ou posterior à data início.");
        }
        if (semestre.getPeriodoMatricula() != null && semestre.getPeriodoMatricula().isEncerrado()) {
            throw new RegraNegocioException("O período deste semestre já foi encerrado.");
        }
        if (!semestres.contains(semestre)) {
            semestres.add(semestre);
        }
        PeriodoMatricula periodo = new PeriodoMatricula(semestre, inicio, fim);
        semestre.setPeriodoMatricula(periodo);
        this.periodoMatriculas = periodo;
        return periodo;
    }

    /**
     * HU13 — encerra o período e avalia cada disciplina (ativa com 3 ou mais alunos).
     */
    public void encerrarPeriodoMatriculas(PeriodoMatricula periodo) {
        if (periodo == null) {
            throw new RegraNegocioException("Nenhum período definido.");
        }
        if (periodo.isEncerrado()) {
            throw new RegraNegocioException("Período já encerrado.");
        }
        periodo.encerrar();
        Semestre semestre = periodo.getSemestre();
        if (semestre != null && semestre.getCurriculo() != null) {
            for (Disciplina disciplina : semestre.getCurriculo().getDisciplinas()) {
                disciplina.avaliarAoFimDoPeriodo(semestre);
            }
        }
    }

    public Curso adicionarCurso(Curso curso) {
        if (curso == null || textoInvalido(curso.getNome())) {
            throw new RegraNegocioException("Informe o nome do curso.");
        }
        if (curso.getNome().contains("|")) {
            throw new RegraNegocioException("O nome do curso não pode conter '|'.");
        }
        if (curso.getNumeroCreditos() <= 0) {
            throw new RegraNegocioException("O número de créditos deve ser maior que zero.");
        }
        for (Curso existente : cursos) {
            if (existente.getNome().equalsIgnoreCase(curso.getNome())) {
                throw new RegraNegocioException("Já existe curso com este nome.");
            }
        }
        cursos.add(curso);
        return curso;
    }

    public Curso cadastrarCurso(String nome, int numeroCreditos) {
        return adicionarCurso(new Curso(nome, numeroCreditos));
    }

    public void adicionarProfessor(Professor professor) {
        validarNovoUsuario(professor);
        professores.add(professor);
    }

    public Professor cadastrarProfessor(String nome, String login, String senha) {
        Professor professor = new Professor(proximoId("p"), nome, login, senha);
        adicionarProfessor(professor);
        return professor;
    }

    public void adicionarAluno(Aluno aluno) {
        validarNovoUsuario(aluno);
        if (textoInvalido(aluno.getMatricula()) || aluno.getMatricula().contains("|")) {
            throw new RegraNegocioException("Informe a matrícula do aluno.");
        }
        for (Aluno existente : alunos) {
            if (existente.getMatricula().equalsIgnoreCase(aluno.getMatricula())) {
                throw new RegraNegocioException("Matrícula já cadastrada.");
            }
        }
        if (aluno.getCurso() == null || !cursos.contains(aluno.getCurso())) {
            throw new RegraNegocioException("Curso do aluno não cadastrado.");
        }
        alunos.add(aluno);
    }

    public Aluno cadastrarAluno(String nome, String login, String senha, String matricula, Curso curso) {
        Aluno aluno = new Aluno(proximoId("a"), nome, login, senha, matricula, curso);
        adicionarAluno(aluno);
        return aluno;
    }

    public Semestre abrirSemestre(int ano, int periodo) {
        if (periodo != 1 && periodo != 2) {
            throw new RegraNegocioException("O período do semestre deve ser 1 ou 2.");
        }
        if (ano < 2000) {
            throw new RegraNegocioException("Informe um ano válido.");
        }
        for (Semestre existente : semestres) {
            if (existente.getAno() == ano && existente.getPeriodo() == periodo) {
                return existente;
            }
        }
        Semestre semestre = new Semestre(ano, periodo);
        semestres.add(semestre);
        return semestre;
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

    private void validarNovoUsuario(Usuario usuario) {
        if (usuario == null || textoInvalido(usuario.getNome()) || textoInvalido(usuario.getLogin())
                || textoInvalido(usuario.getSenha())) {
            throw new RegraNegocioException("Informe nome, login e senha.");
        }
        if (usuario.getNome().contains("|") || usuario.getLogin().contains("|")
                || usuario.getSenha().contains("|") || usuario.getLogin().contains(" ")) {
            throw new RegraNegocioException("Login não pode conter espaço ou '|'.");
        }
        if (mesmoLogin(getLogin(), usuario.getLogin())) {
            throw new RegraNegocioException("Login já cadastrado.");
        }
        for (Professor professor : professores) {
            if (professor != usuario && mesmoLogin(professor.getLogin(), usuario.getLogin())) {
                throw new RegraNegocioException("Login já cadastrado.");
            }
        }
        for (Aluno aluno : alunos) {
            if (aluno != usuario && mesmoLogin(aluno.getLogin(), usuario.getLogin())) {
                throw new RegraNegocioException("Login já cadastrado.");
            }
        }
    }

    private boolean permaneceEmSemestreEncerrado(Disciplina disciplina) {
        for (Semestre semestre : semestres) {
            PeriodoMatricula periodo = semestre.getPeriodoMatricula();
            if (periodo != null && periodo.isEncerrado()
                    && semestre.getCurriculo() != null
                    && semestre.getCurriculo().getDisciplinas().contains(disciplina)) {
                return true;
            }
        }
        return false;
    }

    private boolean mesmoLogin(String existente, String informado) {
        return existente != null && informado != null && existente.equalsIgnoreCase(informado);
    }

    private String proximoId(String prefixo) {
        int maior = 0;
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.addAll(professores);
        usuarios.addAll(alunos);
        for (Usuario usuario : usuarios) {
            String id = usuario.getId();
            if (id != null && id.startsWith(prefixo)) {
                try {
                    maior = Math.max(maior, Integer.parseInt(id.substring(prefixo.length())));
                } catch (NumberFormatException ignorado) {
                    maior++;
                }
            }
        }
        return prefixo + (maior + 1);
    }

    private boolean textoInvalido(String valor) {
        return valor == null || valor.isBlank();
    }
}
