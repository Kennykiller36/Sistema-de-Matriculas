package br.pucminas.matriculas.persistencia;

import br.pucminas.matriculas.Aluno;
import br.pucminas.matriculas.Curriculo;
import br.pucminas.matriculas.Curso;
import br.pucminas.matriculas.Disciplina;
import br.pucminas.matriculas.Matricula;
import br.pucminas.matriculas.PeriodoMatricula;
import br.pucminas.matriculas.Professor;
import br.pucminas.matriculas.RegraNegocioException;
import br.pucminas.matriculas.Secretaria;
import br.pucminas.matriculas.Semestre;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistência em arquivos de texto (pasta {@code dados}).
 */
public class PersistenciaArquivo {

    private final Path diretorio;

    public PersistenciaArquivo(Path diretorio) {
        this.diretorio = diretorio;
    }

    public boolean existe() {
        return Files.exists(diretorio.resolve("secretaria.txt"));
    }

    public void salvar(Secretaria secretaria, List<String> cobrancas) {
        try {
            Files.createDirectories(diretorio);
            escrever("secretaria.txt", List.of(juntar(
                    secretaria.getId(), secretaria.getNome(), secretaria.getLogin(), secretaria.getSenha())));

            List<String> cursos = new ArrayList<>();
            for (Curso curso : secretaria.getCursos()) {
                cursos.add(juntar(curso.getNome(), Integer.toString(curso.getNumeroCreditos())));
            }
            escrever("cursos.txt", cursos);

            List<String> professores = new ArrayList<>();
            for (Professor professor : secretaria.getProfessores()) {
                professores.add(juntar(
                        professor.getId(), professor.getNome(), professor.getLogin(), professor.getSenha()));
            }
            escrever("professores.txt", professores);

            List<String> alunos = new ArrayList<>();
            for (Aluno aluno : secretaria.getAlunos()) {
                alunos.add(juntar(
                        aluno.getId(),
                        aluno.getNome(),
                        aluno.getLogin(),
                        aluno.getSenha(),
                        aluno.getMatricula(),
                        aluno.getCurso().getNome()));
            }
            escrever("alunos.txt", alunos);

            List<String> disciplinas = new ArrayList<>();
            for (Disciplina disciplina : todasDisciplinas(secretaria)) {
                String loginProfessor = disciplina.getProfessor() == null
                        ? ""
                        : disciplina.getProfessor().getLogin();
                boolean noCatalogo = secretaria.getDisciplinas().contains(disciplina);
                disciplinas.add(juntar(
                        disciplina.getCodigo(),
                        disciplina.getNome(),
                        disciplina.getCurso().getNome(),
                        loginProfessor,
                        Integer.toString(disciplina.getCapacidadeMaxima()),
                        Integer.toString(disciplina.getMinimoAlunos()),
                        Boolean.toString(disciplina.isInscricoesEncerradas()),
                        disciplina.getSituacao(),
                        Boolean.toString(noCatalogo)));
            }
            escrever("disciplinas.txt", disciplinas);

            List<String> semestres = new ArrayList<>();
            List<String> curriculos = new ArrayList<>();
            List<String> periodos = new ArrayList<>();
            for (Semestre semestre : secretaria.getSemestres()) {
                semestres.add(juntar(
                        Integer.toString(semestre.getAno()), Integer.toString(semestre.getPeriodo())));
                if (semestre.getCurriculo() != null) {
                    curriculos.add(juntar(
                            Integer.toString(semestre.getAno()),
                            Integer.toString(semestre.getPeriodo()),
                            codigos(semestre.getCurriculo())));
                }
                PeriodoMatricula periodo = semestre.getPeriodoMatricula();
                if (periodo != null) {
                    periodos.add(juntar(
                            Integer.toString(semestre.getAno()),
                            Integer.toString(semestre.getPeriodo()),
                            periodo.getDataInicio().toString(),
                            periodo.getDataFim().toString(),
                            Boolean.toString(periodo.isEncerrado())));
                }
            }
            escrever("semestres.txt", semestres);
            escrever("curriculos.txt", curriculos);
            escrever("periodos.txt", periodos);
            escrever("ofertas.txt", ofertas(secretaria));

            List<String> matriculas = new ArrayList<>();
            for (Aluno aluno : secretaria.getAlunos()) {
                for (Matricula matricula : aluno.getMatriculas()) {
                    matriculas.add(juntar(
                            aluno.getMatricula(),
                            matricula.getDisciplina().getCodigo(),
                            Integer.toString(matricula.getSemestre().getAno()),
                            Integer.toString(matricula.getSemestre().getPeriodo()),
                            matricula.getTipo(),
                            matricula.getSituacao()));
                }
            }
            escrever("matriculas.txt", matriculas);
            escrever("cobrancas.txt", cobrancas);
        } catch (IOException e) {
            throw new RegraNegocioException("Não foi possível gravar os dados: " + e.getMessage());
        }
    }

    public Secretaria carregar(SistemaCobrancaArquivo cobranca) {
        try {
            String[] dadosSecretaria = colunas(primeiraLinha("secretaria.txt"), 4);
            Secretaria secretaria = new Secretaria(
                    dadosSecretaria[0], dadosSecretaria[1], dadosSecretaria[2], dadosSecretaria[3]);

            for (String linha : ler("cursos.txt")) {
                String[] colunas = colunas(linha, 2);
                secretaria.adicionarCurso(new Curso(colunas[0], Integer.parseInt(colunas[1])));
            }
            for (String linha : ler("professores.txt")) {
                String[] colunas = colunas(linha, 4);
                secretaria.adicionarProfessor(new Professor(colunas[0], colunas[1], colunas[2], colunas[3]));
            }
            for (String linha : ler("alunos.txt")) {
                String[] colunas = colunas(linha, 6);
                Curso curso = buscarCurso(secretaria, colunas[5]);
                Aluno aluno = new Aluno(colunas[0], colunas[1], colunas[2], colunas[3], colunas[4], curso);
                aluno.setSistemaCobranca(cobranca);
                secretaria.adicionarAluno(aluno);
            }
            Map<String, Disciplina> disciplinasPorCodigo = new LinkedHashMap<>();
            for (String linha : ler("disciplinas.txt")) {
                String[] colunas = colunas(linha, 9);
                Curso curso = buscarCurso(secretaria, colunas[2]);
                Professor professor = colunas[3].isBlank() ? null : buscarProfessor(secretaria, colunas[3]);
                Disciplina disciplina = new Disciplina(colunas[0], colunas[1], curso, professor);
                disciplina.setCapacidadeMaxima(Integer.parseInt(colunas[4]));
                disciplina.setMinimoAlunos(Integer.parseInt(colunas[5]));
                disciplina.setInscricoesEncerradas(Boolean.parseBoolean(colunas[6]));
                disciplina.setSituacao(colunas[7]);
                if (Boolean.parseBoolean(colunas[8])) {
                    secretaria.cadastrarDisciplina(disciplina);
                }
                disciplinasPorCodigo.put(disciplina.getCodigo(), disciplina);
            }
            for (String linha : ler("semestres.txt")) {
                String[] colunas = colunas(linha, 2);
                secretaria.abrirSemestre(Integer.parseInt(colunas[0]), Integer.parseInt(colunas[1]));
            }
            for (String linha : ler("curriculos.txt")) {
                String[] colunas = colunas(linha, 3);
                Semestre semestre = buscarSemestre(
                        secretaria, Integer.parseInt(colunas[0]), Integer.parseInt(colunas[1]));
                Curriculo curriculo = new Curriculo(semestre);
                if (!colunas[2].isBlank()) {
                    for (String codigo : colunas[2].split(",")) {
                        Disciplina disciplina = disciplinasPorCodigo.get(codigo);
                        if (disciplina == null) {
                            throw new RegraNegocioException("Disciplina não encontrada: " + codigo);
                        }
                        curriculo.adicionarDisciplina(disciplina);
                    }
                }
                semestre.setCurriculo(curriculo);
            }
            vincularProfessoresDosCurriculos(secretaria);
            PeriodoMatricula periodoAtual = null;
            for (String linha : ler("periodos.txt")) {
                String[] colunas = colunas(linha, 5);
                Semestre semestre = buscarSemestre(
                        secretaria, Integer.parseInt(colunas[0]), Integer.parseInt(colunas[1]));
                PeriodoMatricula periodo = secretaria.definirPeriodoMatriculas(
                        semestre, LocalDate.parse(colunas[2]), LocalDate.parse(colunas[3]));
                periodo.setEncerrado(Boolean.parseBoolean(colunas[4]));
                if (!periodo.isEncerrado() || periodoAtual == null) {
                    periodoAtual = periodo;
                }
            }
            secretaria.setPeriodoMatriculas(periodoAtual);
            carregarOfertas(secretaria);

            for (String linha : ler("matriculas.txt")) {
                String[] colunas = colunas(linha, 6);
                Aluno aluno = buscarAluno(secretaria, colunas[0]);
                Disciplina disciplina = disciplinasPorCodigo.get(colunas[1]);
                if (disciplina == null) {
                    throw new RegraNegocioException("Disciplina não encontrada: " + colunas[1]);
                }
                Semestre semestre = buscarSemestre(
                        secretaria, Integer.parseInt(colunas[2]), Integer.parseInt(colunas[3]));
                Matricula matricula = new Matricula(aluno, disciplina, semestre, colunas[4]);
                matricula.setSituacao(colunas[5]);
                aluno.getMatriculas().add(matricula);
                disciplina.getMatriculas().add(matricula);
            }

            cobranca.carregar(ler("cobrancas.txt"));
            return secretaria;
        } catch (IOException e) {
            throw new RegraNegocioException("Não foi possível ler os dados: " + e.getMessage());
        } catch (RuntimeException e) {
            if (e instanceof RegraNegocioException) {
                throw e;
            }
            throw new RegraNegocioException("Arquivo de dados inválido: " + e.getMessage());
        }
    }

    private void vincularProfessoresDosCurriculos(Secretaria secretaria) {
        for (Semestre semestre : secretaria.getSemestres()) {
            if (semestre.getCurriculo() == null) {
                continue;
            }
            for (Disciplina disciplina : semestre.getCurriculo().getDisciplinas()) {
                Professor professor = disciplina.getProfessor();
                if (professor != null && !professor.getDisciplinas().contains(disciplina)) {
                    professor.getDisciplinas().add(disciplina);
                }
            }
        }
    }

    private List<String> ofertas(Secretaria secretaria) {
        List<String> linhas = new ArrayList<>();
        for (Semestre semestre : secretaria.getSemestres()) {
            if (semestre.getCurriculo() == null) {
                continue;
            }
            for (Disciplina disciplina : semestre.getCurriculo().getDisciplinas()) {
                String situacao = semestre.getSituacaoOferta(disciplina.getCodigo());
                if (situacao == null) {
                    situacao = disciplina.getSituacao();
                }
                Boolean encerradas = semestre.getInscricoesEncerradasOferta(disciplina.getCodigo());
                boolean inscricoesEncerradas = encerradas != null
                        ? encerradas
                        : disciplina.isInscricoesEncerradas();
                linhas.add(juntar(
                        Integer.toString(semestre.getAno()),
                        Integer.toString(semestre.getPeriodo()),
                        disciplina.getCodigo(),
                        situacao,
                        Boolean.toString(inscricoesEncerradas)));
            }
        }
        return linhas;
    }

    private void carregarOfertas(Secretaria secretaria) throws IOException {
        List<String> linhas = ler("ofertas.txt");
        if (linhas.isEmpty()) {
            for (Semestre semestre : secretaria.getSemestres()) {
                if (semestre.getCurriculo() == null) {
                    continue;
                }
                for (Disciplina disciplina : semestre.getCurriculo().getDisciplinas()) {
                    semestre.definirOferta(
                            disciplina.getCodigo(),
                            disciplina.getSituacao(),
                            disciplina.isInscricoesEncerradas());
                }
            }
            return;
        }
        for (String linha : linhas) {
            String[] colunas = colunas(linha, 5);
            Semestre semestre = buscarSemestre(
                    secretaria, Integer.parseInt(colunas[0]), Integer.parseInt(colunas[1]));
            semestre.definirOferta(colunas[2], colunas[3], Boolean.parseBoolean(colunas[4]));
        }
    }

    private Curso buscarCurso(Secretaria secretaria, String nome) {
        for (Curso curso : secretaria.getCursos()) {
            if (curso.getNome().equals(nome)) {
                return curso;
            }
        }
        throw new RegraNegocioException("Curso não encontrado: " + nome);
    }

    private Professor buscarProfessor(Secretaria secretaria, String login) {
        for (Professor professor : secretaria.getProfessores()) {
            if (professor.getLogin().equals(login)) {
                return professor;
            }
        }
        throw new RegraNegocioException("Professor não encontrado: " + login);
    }

    private Aluno buscarAluno(Secretaria secretaria, String matricula) {
        for (Aluno aluno : secretaria.getAlunos()) {
            if (aluno.getMatricula().equals(matricula)) {
                return aluno;
            }
        }
        throw new RegraNegocioException("Aluno não encontrado: " + matricula);
    }

    private Semestre buscarSemestre(Secretaria secretaria, int ano, int periodo) {
        for (Semestre semestre : secretaria.getSemestres()) {
            if (semestre.getAno() == ano && semestre.getPeriodo() == periodo) {
                return semestre;
            }
        }
        throw new RegraNegocioException("Semestre não encontrado: " + ano + "/" + periodo);
    }

    private List<Disciplina> todasDisciplinas(Secretaria secretaria) {
        List<Disciplina> todas = new ArrayList<>();
        todas.addAll(secretaria.getDisciplinas());
        for (Semestre semestre : secretaria.getSemestres()) {
            if (semestre.getCurriculo() == null) {
                continue;
            }
            for (Disciplina disciplina : semestre.getCurriculo().getDisciplinas()) {
                if (!todas.contains(disciplina)) {
                    todas.add(disciplina);
                }
            }
        }
        for (Aluno aluno : secretaria.getAlunos()) {
            for (Matricula matricula : aluno.getMatriculas()) {
                if (!todas.contains(matricula.getDisciplina())) {
                    todas.add(matricula.getDisciplina());
                }
            }
        }
        return todas;
    }

    private String codigos(Curriculo curriculo) {
        StringBuilder codigos = new StringBuilder();
        for (Disciplina disciplina : curriculo.getDisciplinas()) {
            if (codigos.length() > 0) {
                codigos.append(',');
            }
            codigos.append(disciplina.getCodigo());
        }
        return codigos.toString();
    }

    private void escrever(String arquivo, List<String> linhas) throws IOException {
        Files.write(diretorio.resolve(arquivo), linhas, StandardCharsets.UTF_8);
    }

    private List<String> ler(String arquivo) throws IOException {
        Path caminho = diretorio.resolve(arquivo);
        if (!Files.exists(caminho)) {
            return List.of();
        }
        List<String> linhas = new ArrayList<>();
        for (String linha : Files.readAllLines(caminho, StandardCharsets.UTF_8)) {
            if (!linha.isBlank()) {
                linhas.add(linha);
            }
        }
        return linhas;
    }

    private String primeiraLinha(String arquivo) throws IOException {
        List<String> linhas = ler(arquivo);
        if (linhas.isEmpty()) {
            throw new RegraNegocioException("Arquivo vazio: " + arquivo);
        }
        return linhas.get(0);
    }

    private String[] colunas(String linha, int esperadas) {
        String[] partes = linha.split("\\|", -1);
        if (partes.length != esperadas) {
            throw new RegraNegocioException("Linha inválida: " + linha);
        }
        return partes;
    }

    private String juntar(String... campos) {
        return String.join("|", campos);
    }
}
