package br.pucminas.matriculas;

import br.pucminas.matriculas.persistencia.PersistenciaArquivo;
import br.pucminas.matriculas.persistencia.SistemaCobrancaArquivo;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Coordena o domínio e grava o estado em arquivo após cada alteração.
 */
public class Aplicacao {

    private final PersistenciaArquivo persistencia;
    private final SistemaCobrancaArquivo cobranca;
    private final Secretaria secretaria;
    private final boolean primeiroAcesso;

    public Aplicacao(Path diretorio) {
        this.persistencia = new PersistenciaArquivo(diretorio);
        this.cobranca = new SistemaCobrancaArquivo();
        if (persistencia.existe()) {
            this.secretaria = persistencia.carregar(cobranca);
            this.primeiroAcesso = false;
        } else {
            this.secretaria = new Secretaria("s1", "Maria Oliveira", "secretaria", "senha123");
            DadosIniciais.popular(secretaria, cobranca);
            persistencia.salvar(secretaria, cobranca.getRegistros());
            this.primeiroAcesso = true;
        }
    }

    public Usuario autenticar(String login, String senha) {
        if (login == null || senha == null) {
            return null;
        }
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(secretaria);
        usuarios.addAll(secretaria.getProfessores());
        usuarios.addAll(secretaria.getAlunos());
        for (Usuario usuario : usuarios) {
            if (usuario.autenticar(login, senha)) {
                return usuario;
            }
        }
        return null;
    }

    public List<Semestre> semestresAbertos() {
        List<Semestre> abertos = new ArrayList<>();
        LocalDate hoje = LocalDate.now();
        for (Semestre semestre : secretaria.getSemestres()) {
            PeriodoMatricula periodo = semestre.getPeriodoMatricula();
            if (periodo != null && periodo.estaAberto(hoje)) {
                abertos.add(semestre);
            }
        }
        return abertos;
    }

    public Matricula matricularObrigatoria(Aluno aluno, Disciplina disciplina, Semestre semestre) {
        Matricula matricula = aluno.matricularObrigatoria(disciplina, semestre);
        salvar();
        return matricula;
    }

    public Matricula matricularOptativa(Aluno aluno, Disciplina disciplina, Semestre semestre) {
        Matricula matricula = aluno.matricularOptativa(disciplina, semestre);
        salvar();
        return matricula;
    }

    public void cancelarMatricula(Aluno aluno, Matricula matricula) {
        aluno.cancelarMatricula(matricula);
        salvar();
    }

    public void finalizarMatricula(Aluno aluno, Semestre semestre) {
        aluno.finalizarMatricula(semestre);
        salvar();
    }

    public void cadastrarDisciplina(Disciplina disciplina) {
        secretaria.cadastrarDisciplina(disciplina);
        salvar();
    }

    public void excluirDisciplina(Disciplina disciplina) {
        secretaria.excluirDisciplina(disciplina);
        salvar();
    }

    public Curriculo gerarCurriculo(Semestre semestre, List<Disciplina> disciplinas) {
        Curriculo curriculo = secretaria.gerarCurriculo(semestre, disciplinas);
        salvar();
        return curriculo;
    }

    public PeriodoMatricula definirPeriodo(Semestre semestre, LocalDate inicio, LocalDate fim) {
        PeriodoMatricula periodo = secretaria.definirPeriodoMatriculas(semestre, inicio, fim);
        salvar();
        return periodo;
    }

    public void encerrarPeriodo(PeriodoMatricula periodo) {
        secretaria.encerrarPeriodoMatriculas(periodo);
        salvar();
    }

    public Curso cadastrarCurso(String nome, int creditos) {
        Curso curso = secretaria.cadastrarCurso(nome, creditos);
        salvar();
        return curso;
    }

    public Professor cadastrarProfessor(String nome, String login, String senha) {
        Professor professor = secretaria.cadastrarProfessor(nome, login, senha);
        salvar();
        return professor;
    }

    public Aluno cadastrarAluno(String nome, String login, String senha, String matricula, Curso curso) {
        Aluno aluno = secretaria.cadastrarAluno(nome, login, senha, matricula, curso);
        aluno.setSistemaCobranca(cobranca);
        salvar();
        return aluno;
    }

    public Semestre abrirSemestre(int ano, int periodo) {
        Semestre semestre = secretaria.abrirSemestre(ano, periodo);
        salvar();
        return semestre;
    }

    public Secretaria getSecretaria() {
        return secretaria;
    }

    public SistemaCobrancaArquivo getCobranca() {
        return cobranca;
    }

    public boolean isPrimeiroAcesso() {
        return primeiroAcesso;
    }

    private void salvar() {
        persistencia.salvar(secretaria, cobranca.getRegistros());
    }
}
