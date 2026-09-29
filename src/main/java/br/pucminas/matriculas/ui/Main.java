package br.pucminas.matriculas.ui;

import br.pucminas.matriculas.Aluno;
import br.pucminas.matriculas.Aplicacao;
import br.pucminas.matriculas.Curso;
import br.pucminas.matriculas.Disciplina;
import br.pucminas.matriculas.Matricula;
import br.pucminas.matriculas.PeriodoMatricula;
import br.pucminas.matriculas.Professor;
import br.pucminas.matriculas.RegraNegocioException;
import br.pucminas.matriculas.Secretaria;
import br.pucminas.matriculas.Semestre;
import br.pucminas.matriculas.Usuario;

import java.io.Console;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interface de linha de comando do protótipo (Lab01S03).
 */
public class Main {

    private final Aplicacao aplicacao;
    private final Scanner scanner;

    public Main(Aplicacao aplicacao, Scanner scanner) {
        this.aplicacao = aplicacao;
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        Charset charset = charsetDoTerminal();
        System.setOut(new PrintStream(System.out, true, charset));
        System.setErr(new PrintStream(System.err, true, charset));
        Path dados = args.length > 0 ? Path.of(args[0]) : Path.of("dados");
        Aplicacao aplicacao = new Aplicacao(dados);
        System.out.println(aplicacao.isPrimeiroAcesso()
                ? "Primeiro acesso. Dados de demonstração gravados em " + dados.toAbsolutePath()
                : "Dados carregados de " + dados.toAbsolutePath());
        new Main(aplicacao, new Scanner(System.in, charset)).executar();
    }

    /**
     * O PowerShell do Windows costuma usar a página CP850, diferente do UTF-8.
     */
    private static Charset charsetDoTerminal() {
        Charset pagina = paginaDeCodigoWindows();
        if (pagina != null) {
            return pagina;
        }
        Console console = System.console();
        if (console != null) {
            return console.charset();
        }
        return Charset.defaultCharset();
    }

    private static Charset paginaDeCodigoWindows() {
        String sistema = System.getProperty("os.name", "");
        if (!sistema.toLowerCase(Locale.ROOT).contains("win")) {
            return null;
        }
        try {
            Process processo = new ProcessBuilder("cmd.exe", "/c", "chcp")
                    .redirectErrorStream(true)
                    .start();
            String saida = new String(processo.getInputStream().readAllBytes(), Charset.defaultCharset());
            if (processo.waitFor() != 0) {
                return null;
            }
            Matcher numeros = Pattern.compile("(\\d+)").matcher(saida);
            if (!numeros.find()) {
                return null;
            }
            int codigo = Integer.parseInt(numeros.group(1));
            if (codigo == 65001) {
                return StandardCharsets.UTF_8;
            }
            return Charset.forName("Cp" + codigo);
        } catch (Exception e) {
            return null;
        }
    }

    public void executar() {
        System.out.println();
        System.out.println("========================================");
        System.out.println(" Sistema de Matrículas - PUC Minas");
        System.out.println("========================================");
        System.out.println("Contas de demonstração (senha123):");
        System.out.println("  secretaria | ana | carlos | joao | maria | pedro");
        System.out.println();

        while (true) {
            String login = ler("Login (ou sair): ");
            if (login == null) {
                System.out.println();
                System.out.println("Até logo.");
                return;
            }
            if (login.isBlank()) {
                continue;
            }
            if (login.equalsIgnoreCase("sair")) {
                System.out.println("Até logo.");
                return;
            }
            String senha = ler("Senha: ");
            if (senha == null) {
                return;
            }
            Usuario usuario = aplicacao.autenticar(login, senha);
            if (usuario == null) {
                System.out.println("Login ou senha inválidos.");
                System.out.println();
                continue;
            }
            System.out.println();
            System.out.println("Bem-vindo, " + usuario.getNome() + ".");
            if (usuario instanceof Aluno aluno) {
                menuAluno(aluno);
            } else if (usuario instanceof Professor professor) {
                menuProfessor(professor);
            } else if (usuario instanceof Secretaria) {
                menuSecretaria();
            }
            System.out.println();
        }
    }

    private void menuAluno(Aluno aluno) {
        while (true) {
            System.out.println();
            System.out.println("--- Aluno: " + aluno.getNome() + " (" + aluno.getMatricula() + ") ---");
            System.out.println("1. Ver disciplinas ofertadas");
            System.out.println("2. Matricular em obrigatória (até " + Aluno.MAX_OBRIGATORIAS + ")");
            System.out.println("3. Matricular em optativa (até " + Aluno.MAX_OPTATIVAS + ")");
            System.out.println("4. Cancelar matrícula");
            System.out.println("5. Ver minhas matrículas");
            System.out.println("6. Finalizar matrícula e notificar cobrança");
            System.out.println("0. Voltar ao login");
            String opcao = ler("Opção: ");
            if (opcao == null) {
                return;
            }
            if (opcao.isBlank()) {
                continue;
            }
            if (opcao.equals("0")) {
                return;
            }
            try {
                switch (opcao) {
                    case "1" -> verOferta();
                    case "2" -> matricular(aluno, true);
                    case "3" -> matricular(aluno, false);
                    case "4" -> cancelar(aluno);
                    case "5" -> verMatriculas(aluno);
                    case "6" -> finalizar(aluno);
                    default -> System.out.println("Opção inválida.");
                }
            } catch (RegraNegocioException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void menuProfessor(Professor professor) {
        while (true) {
            System.out.println();
            System.out.println("--- Professor: " + professor.getNome() + " ---");
            System.out.println("1. Ver alunos das minhas disciplinas");
            System.out.println("0. Voltar ao login");
            String opcao = ler("Opção: ");
            if (opcao == null || opcao.equals("0")) {
                return;
            }
            if (opcao.isBlank()) {
                continue;
            }
            if (!opcao.equals("1")) {
                System.out.println("Opção inválida.");
                continue;
            }
            if (professor.getDisciplinas().isEmpty()) {
                System.out.println("Nenhuma disciplina vinculada.");
                continue;
            }
            Disciplina disciplina = escolherDisciplina(professor.getDisciplinas());
            if (disciplina == null) {
                continue;
            }
            System.out.println(descrever(disciplina));
            List<Aluno> alunos = professor.listarAlunos(disciplina);
            if (alunos.isEmpty()) {
                System.out.println("Nenhum aluno com matrícula ativa.");
                continue;
            }
            for (Aluno aluno : alunos) {
                System.out.println("- " + aluno.getNome() + " (" + aluno.getMatricula() + ")");
            }
        }
    }

    private void menuSecretaria() {
        while (true) {
            System.out.println();
            System.out.println("--- Secretaria ---");
            System.out.println("1. Consultar disciplinas");
            System.out.println("2. Cadastrar disciplina");
            System.out.println("3. Excluir disciplina");
            System.out.println("4. Consultar cursos");
            System.out.println("5. Cadastrar curso");
            System.out.println("6. Cadastrar professor");
            System.out.println("7. Cadastrar aluno");
            System.out.println("8. Gerar currículo");
            System.out.println("9. Definir período de matrículas");
            System.out.println("10. Encerrar período de matrículas");
            System.out.println("11. Consultar cobranças");
            System.out.println("0. Voltar ao login");
            String opcao = ler("Opção: ");
            if (opcao == null || opcao.equals("0")) {
                return;
            }
            if (opcao.isBlank()) {
                continue;
            }
            try {
                switch (opcao) {
                    case "1" -> consultarDisciplinas();
                    case "2" -> cadastrarDisciplina();
                    case "3" -> excluirDisciplina();
                    case "4" -> consultarCursos();
                    case "5" -> cadastrarCurso();
                    case "6" -> cadastrarProfessor();
                    case "7" -> cadastrarAluno();
                    case "8" -> gerarCurriculo();
                    case "9" -> definirPeriodo();
                    case "10" -> encerrarPeriodo();
                    case "11" -> consultarCobrancas();
                    default -> System.out.println("Opção inválida.");
                }
            } catch (RegraNegocioException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void verOferta() {
        List<Semestre> semestres = aplicacao.getSecretaria().getSemestres();
        if (semestres.isEmpty()) {
            System.out.println("Nenhum semestre cadastrado.");
            return;
        }
        for (Semestre semestre : semestres) {
            System.out.println("Semestre " + rotulo(semestre) + " — " + descreverPeriodo(semestre.getPeriodoMatricula()));
            if (semestre.getCurriculo() == null || semestre.getCurriculo().getDisciplinas().isEmpty()) {
                System.out.println("  Sem disciplinas no currículo.");
                continue;
            }
            for (Disciplina disciplina : semestre.getCurriculo().getDisciplinas()) {
                System.out.println("  " + descrever(disciplina));
            }
        }
    }

    private void matricular(Aluno aluno, boolean obrigatoria) {
        Semestre semestre = escolherSemestreAberto();
        if (semestre == null) {
            return;
        }
        if (semestre.getCurriculo() == null) {
            System.out.println("Este semestre não tem currículo.");
            return;
        }
        List<Disciplina> ofertadas = new ArrayList<>();
        for (Disciplina disciplina : semestre.getCurriculo().getDisciplinas()) {
            if (disciplina.getCurso() != null && aluno.getCurso() != null
                    && disciplina.getCurso().getNome().equals(aluno.getCurso().getNome())) {
                ofertadas.add(disciplina);
            }
        }
        Disciplina disciplina = escolherDisciplina(ofertadas);
        if (disciplina == null) {
            return;
        }
        Matricula matricula = obrigatoria
                ? aplicacao.matricularObrigatoria(aluno, disciplina, semestre)
                : aplicacao.matricularOptativa(aluno, disciplina, semestre);
        System.out.println("Matrícula realizada em " + matricula.getDisciplina().getCodigo()
                + " (" + rotuloTipo(matricula.getTipo()) + ").");
        mostrarUltimaCobranca();
    }

    private void cancelar(Aluno aluno) {
        List<Matricula> ativas = new ArrayList<>();
        for (Matricula matricula : aluno.getMatriculas()) {
            if (matricula.isAtiva()) {
                ativas.add(matricula);
            }
        }
        if (ativas.isEmpty()) {
            System.out.println("Não há matrícula ativa para cancelar.");
            return;
        }
        for (int i = 0; i < ativas.size(); i++) {
            Matricula matricula = ativas.get(i);
            System.out.println((i + 1) + ". " + matricula.getDisciplina().getCodigo()
                    + " - " + matricula.getDisciplina().getNome()
                    + " | " + rotuloTipo(matricula.getTipo())
                    + " | " + rotulo(matricula.getSemestre()));
        }
        String texto = ler("Matrícula (número ou enter para voltar): ");
        if (texto == null || texto.isBlank()) {
            return;
        }
        int indice;
        try {
            indice = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            System.out.println("Informe o número da matrícula.");
            return;
        }
        if (indice < 1 || indice > ativas.size()) {
            System.out.println("Matrícula não encontrada.");
            return;
        }
        aplicacao.cancelarMatricula(aluno, ativas.get(indice - 1));
        System.out.println("Matrícula cancelada. A vaga voltou para a disciplina.");
        mostrarUltimaCobranca();
    }

    private void verMatriculas(Aluno aluno) {
        if (aluno.getMatriculas().isEmpty()) {
            System.out.println("Nenhuma matrícula registrada.");
            return;
        }
        for (Matricula matricula : aluno.getMatriculas()) {
            System.out.println("- " + matricula.getDisciplina().getCodigo()
                    + " - " + matricula.getDisciplina().getNome()
                    + " | " + rotuloTipo(matricula.getTipo())
                    + " | " + rotuloSituacaoMatricula(matricula.getSituacao())
                    + " | " + rotulo(matricula.getSemestre()));
        }
    }

    private void finalizar(Aluno aluno) {
        Semestre semestre = escolherSemestre(aplicacao.getSecretaria().getSemestres());
        if (semestre == null) {
            return;
        }
        aplicacao.finalizarMatricula(aluno, semestre);
        System.out.println("Matrícula do semestre " + rotulo(semestre) + " finalizada.");
        mostrarUltimaCobranca();
    }

    private void consultarDisciplinas() {
        List<Disciplina> disciplinas = aplicacao.getSecretaria().consultarDisciplinas();
        if (disciplinas.isEmpty()) {
            System.out.println("Nenhuma disciplina cadastrada.");
            return;
        }
        for (Disciplina disciplina : disciplinas) {
            System.out.println(descrever(disciplina)
                    + " | curso " + disciplina.getCurso().getNome());
        }
    }

    private void cadastrarDisciplina() {
        Curso curso = escolherCurso();
        if (curso == null) {
            return;
        }
        Professor professor = escolherProfessor();
        if (professor == null) {
            return;
        }
        String codigo = ler("Código: ");
        String nome = ler("Nome: ");
        if (codigo == null || nome == null) {
            return;
        }
        aplicacao.cadastrarDisciplina(new Disciplina(codigo, nome, curso, professor));
        System.out.println("Disciplina cadastrada.");
    }

    private void excluirDisciplina() {
        Disciplina disciplina = escolherDisciplina(aplicacao.getSecretaria().consultarDisciplinas());
        if (disciplina == null) {
            return;
        }
        aplicacao.excluirDisciplina(disciplina);
        System.out.println("Disciplina excluída das consultas e dos currículos ainda não encerrados.");
    }

    private void consultarCursos() {
        List<Curso> cursos = aplicacao.getSecretaria().getCursos();
        if (cursos.isEmpty()) {
            System.out.println("Nenhum curso cadastrado.");
            return;
        }
        for (Curso curso : cursos) {
            System.out.println("- " + curso.getNome() + " (" + curso.getNumeroCreditos() + " créditos, "
                    + curso.getDisciplinas().size() + " disciplinas)");
        }
    }

    private void cadastrarCurso() {
        String nome = ler("Nome do curso: ");
        if (nome == null) {
            return;
        }
        Integer creditos = lerInteiro("Número de créditos: ");
        if (creditos == null) {
            return;
        }
        aplicacao.cadastrarCurso(nome, creditos);
        System.out.println("Curso cadastrado.");
    }

    private void cadastrarProfessor() {
        String nome = ler("Nome: ");
        String login = ler("Login: ");
        String senha = ler("Senha: ");
        if (nome == null || login == null || senha == null) {
            return;
        }
        Professor professor = aplicacao.cadastrarProfessor(nome, login, senha);
        System.out.println("Professor cadastrado. Login: " + professor.getLogin());
    }

    private void cadastrarAluno() {
        Curso curso = escolherCurso();
        if (curso == null) {
            return;
        }
        String nome = ler("Nome: ");
        String login = ler("Login: ");
        String senha = ler("Senha: ");
        String matricula = ler("Matrícula: ");
        if (nome == null || login == null || senha == null || matricula == null) {
            return;
        }
        Aluno aluno = aplicacao.cadastrarAluno(nome, login, senha, matricula, curso);
        System.out.println("Aluno cadastrado. Login: " + aluno.getLogin());
    }

    private void gerarCurriculo() {
        Semestre semestre = escolherOuCriarSemestre();
        if (semestre == null) {
            return;
        }
        List<Disciplina> catalogo = aplicacao.getSecretaria().consultarDisciplinas();
        if (catalogo.isEmpty()) {
            System.out.println("Cadastre disciplinas antes de gerar o currículo.");
            return;
        }
        System.out.println("Disciplinas disponíveis:");
        for (int i = 0; i < catalogo.size(); i++) {
            System.out.println((i + 1) + ". " + catalogo.get(i).getCodigo() + " - " + catalogo.get(i).getNome());
        }
        String texto = ler("Códigos ou números separados por vírgula: ");
        if (texto == null || texto.isBlank()) {
            return;
        }
        List<Disciplina> selecionadas = new ArrayList<>();
        for (String token : texto.split("[,\\s]+")) {
            if (token.isBlank()) {
                continue;
            }
            Disciplina disciplina = localizarDisciplina(catalogo, token);
            if (disciplina == null) {
                throw new RegraNegocioException("Disciplina não encontrada: " + token);
            }
            if (!selecionadas.contains(disciplina)) {
                selecionadas.add(disciplina);
            }
        }
        aplicacao.gerarCurriculo(semestre, selecionadas);
        System.out.println("Currículo do semestre " + rotulo(semestre) + " gerado com "
                + selecionadas.size() + " disciplina(s).");
    }

    private void definirPeriodo() {
        Semestre semestre = escolherOuCriarSemestre();
        if (semestre == null) {
            return;
        }
        LocalDate inicio = lerData("Data de início");
        LocalDate fim = lerData("Data de fim");
        if (inicio == null || fim == null) {
            return;
        }
        aplicacao.definirPeriodo(semestre, inicio, fim);
        System.out.println("Período de matrículas definido para " + rotulo(semestre) + ".");
    }

    private void encerrarPeriodo() {
        List<PeriodoMatricula> abertos = new ArrayList<>();
        for (Semestre semestre : aplicacao.getSecretaria().getSemestres()) {
            PeriodoMatricula periodo = semestre.getPeriodoMatricula();
            if (periodo != null && !periodo.isEncerrado()) {
                abertos.add(periodo);
            }
        }
        if (abertos.isEmpty()) {
            System.out.println("Não há período de matrículas em aberto.");
            return;
        }
        PeriodoMatricula periodo;
        if (abertos.size() == 1) {
            periodo = abertos.get(0);
            System.out.println("Período " + rotulo(periodo.getSemestre())
                    + " (" + periodo.getDataInicio() + " a " + periodo.getDataFim() + ").");
        } else {
            for (int i = 0; i < abertos.size(); i++) {
                PeriodoMatricula item = abertos.get(i);
                System.out.println((i + 1) + ". " + rotulo(item.getSemestre()));
            }
            Integer indice = lerInteiro("Período: ");
            if (indice == null || indice < 1 || indice > abertos.size()) {
                System.out.println("Período não encontrado.");
                return;
            }
            periodo = abertos.get(indice - 1);
        }
        String confirma = ler("Encerrar e avaliar as turmas? (s/n): ");
        if (confirma == null || !confirma.equalsIgnoreCase("s")) {
            System.out.println("Encerramento cancelado.");
            return;
        }
        aplicacao.encerrarPeriodo(periodo);
        System.out.println("Período encerrado. Situação das disciplinas:");
        if (periodo.getSemestre().getCurriculo() == null) {
            return;
        }
        for (Disciplina disciplina : periodo.getSemestre().getCurriculo().getDisciplinas()) {
            System.out.println("- " + disciplina.getCodigo() + " -> " + rotuloSituacao(disciplina.getSituacao())
                    + " (" + disciplina.obterQuantidadeInscritos() + " alunos)");
        }
    }

    private void consultarCobrancas() {
        List<String> registros = aplicacao.getCobranca().getRegistros();
        if (registros.isEmpty()) {
            System.out.println("Nenhuma cobrança notificada.");
            return;
        }
        for (String registro : registros) {
            System.out.println(formatarCobranca(registro));
        }
    }

    private Semestre escolherSemestreAberto() {
        List<Semestre> abertos = aplicacao.semestresAbertos();
        if (abertos.isEmpty()) {
            System.out.println("Não há período de matrículas aberto.");
            return null;
        }
        if (abertos.size() == 1) {
            return abertos.get(0);
        }
        return escolherSemestre(abertos);
    }

    private Semestre escolherSemestre(List<Semestre> semestres) {
        if (semestres.isEmpty()) {
            System.out.println("Nenhum semestre cadastrado.");
            return null;
        }
        if (semestres.size() == 1) {
            return semestres.get(0);
        }
        for (int i = 0; i < semestres.size(); i++) {
            System.out.println((i + 1) + ". " + rotulo(semestres.get(i)));
        }
        Integer indice = lerInteiro("Semestre: ");
        if (indice == null || indice < 1 || indice > semestres.size()) {
            System.out.println("Semestre não encontrado.");
            return null;
        }
        return semestres.get(indice - 1);
    }

    private Semestre escolherOuCriarSemestre() {
        List<Semestre> semestres = aplicacao.getSecretaria().getSemestres();
        System.out.println("Semestres:");
        for (int i = 0; i < semestres.size(); i++) {
            System.out.println((i + 1) + ". " + rotulo(semestres.get(i)));
        }
        System.out.println("N. Criar novo semestre");
        String texto = ler("Semestre (número ou N): ");
        if (texto == null || texto.isBlank()) {
            return null;
        }
        if (texto.equalsIgnoreCase("n")) {
            Integer ano = lerInteiro("Ano: ");
            Integer periodo = lerInteiro("Período (1 ou 2): ");
            if (ano == null || periodo == null) {
                return null;
            }
            return aplicacao.abrirSemestre(ano, periodo);
        }
        try {
            int indice = Integer.parseInt(texto);
            if (indice < 1 || indice > semestres.size()) {
                System.out.println("Semestre não encontrado.");
                return null;
            }
            return semestres.get(indice - 1);
        } catch (NumberFormatException e) {
            System.out.println("Informe o número do semestre ou N.");
            return null;
        }
    }

    private Curso escolherCurso() {
        List<Curso> cursos = aplicacao.getSecretaria().getCursos();
        if (cursos.isEmpty()) {
            System.out.println("Nenhum curso cadastrado.");
            return null;
        }
        if (cursos.size() == 1) {
            return cursos.get(0);
        }
        for (int i = 0; i < cursos.size(); i++) {
            System.out.println((i + 1) + ". " + cursos.get(i).getNome());
        }
        Integer indice = lerInteiro("Curso: ");
        if (indice == null || indice < 1 || indice > cursos.size()) {
            System.out.println("Curso não encontrado.");
            return null;
        }
        return cursos.get(indice - 1);
    }

    private Professor escolherProfessor() {
        List<Professor> professores = aplicacao.getSecretaria().getProfessores();
        if (professores.isEmpty()) {
            System.out.println("Nenhum professor cadastrado.");
            return null;
        }
        for (int i = 0; i < professores.size(); i++) {
            System.out.println((i + 1) + ". " + professores.get(i).getNome());
        }
        Integer indice = lerInteiro("Professor: ");
        if (indice == null || indice < 1 || indice > professores.size()) {
            System.out.println("Professor não encontrado.");
            return null;
        }
        return professores.get(indice - 1);
    }

    private Disciplina escolherDisciplina(List<Disciplina> disciplinas) {
        if (disciplinas.isEmpty()) {
            System.out.println("Nenhuma disciplina disponível.");
            return null;
        }
        for (int i = 0; i < disciplinas.size(); i++) {
            System.out.println((i + 1) + ". " + descrever(disciplinas.get(i)));
        }
        while (true) {
            String texto = ler("Disciplina (número, código ou enter para voltar): ");
            if (texto == null || texto.isBlank()) {
                return null;
            }
            Disciplina disciplina = localizarDisciplina(disciplinas, texto);
            if (disciplina != null) {
                return disciplina;
            }
            System.out.println("Disciplina não encontrada.");
        }
    }

    private Disciplina localizarDisciplina(List<Disciplina> disciplinas, String texto) {
        try {
            int indice = Integer.parseInt(texto);
            if (indice >= 1 && indice <= disciplinas.size()) {
                return disciplinas.get(indice - 1);
            }
        } catch (NumberFormatException ignorado) {
            for (Disciplina disciplina : disciplinas) {
                if (disciplina.getCodigo().equalsIgnoreCase(texto)) {
                    return disciplina;
                }
            }
        }
        return null;
    }

    private void mostrarUltimaCobranca() {
        List<String> registros = aplicacao.getCobranca().getRegistros();
        if (!registros.isEmpty()) {
            System.out.println("Cobrança notificada: " + formatarCobranca(registros.get(registros.size() - 1)));
        }
    }

    private String formatarCobranca(String registro) {
        String[] partes = registro.split("\\|", -1);
        if (partes.length < 6) {
            return registro;
        }
        String disciplinas = partes[5].isBlank() ? "nenhuma disciplina" : partes[5];
        return partes[0] + " | " + partes[2] + " (" + partes[1] + ") | semestre "
                + partes[3] + "/" + partes[4] + " | " + disciplinas;
    }

    private String descrever(Disciplina disciplina) {
        String professor = disciplina.getProfessor() == null ? "sem professor" : disciplina.getProfessor().getNome();
        String vagas = disciplina.obterQuantidadeInscritos() + "/" + disciplina.getCapacidadeMaxima() + " inscritos";
        String extra = disciplina.isInscricoesEncerradas() ? " | inscrições encerradas" : "";
        return disciplina.getCodigo() + " - " + disciplina.getNome()
                + " | " + professor
                + " | " + vagas
                + " | " + rotuloSituacao(disciplina.getSituacao())
                + extra;
    }

    private String descreverPeriodo(PeriodoMatricula periodo) {
        if (periodo == null) {
            return "período não definido";
        }
        if (periodo.isEncerrado()) {
            return "período encerrado";
        }
        if (periodo.estaAberto(LocalDate.now())) {
            return "período aberto até " + periodo.getDataFim();
        }
        return "período de " + periodo.getDataInicio() + " a " + periodo.getDataFim();
    }

    private String rotulo(Semestre semestre) {
        return semestre.getAno() + "/" + semestre.getPeriodo();
    }

    private String rotuloSituacao(String situacao) {
        return switch (situacao) {
            case Disciplina.ATIVA -> "ativa";
            case Disciplina.CANCELADA -> "cancelada";
            case Disciplina.EM_INSCRICAO -> "em inscrição";
            default -> situacao;
        };
    }

    private String rotuloSituacaoMatricula(String situacao) {
        return switch (situacao) {
            case Matricula.SITUACAO_ATIVA -> "ativa";
            case Matricula.SITUACAO_CANCELADA -> "cancelada";
            default -> situacao;
        };
    }

    private String rotuloTipo(String tipo) {
        return Matricula.TIPO_OBRIGATORIA.equals(tipo) ? "obrigatória" : "optativa";
    }

    private String ler(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine().trim();
    }

    private Integer lerInteiro(String prompt) {
        String texto = ler(prompt);
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            System.out.println("Informe um número válido.");
            return null;
        }
    }

    private LocalDate lerData(String prompt) {
        String texto = ler(prompt + " (aaaa-mm-dd): ");
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            System.out.println("Data inválida. Use aaaa-mm-dd.");
            return null;
        }
    }
}
