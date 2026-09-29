package br.pucminas.matriculas.persistencia;

import br.pucminas.matriculas.Aluno;
import br.pucminas.matriculas.Disciplina;
import br.pucminas.matriculas.Semestre;
import br.pucminas.matriculas.SistemaCobranca;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Notifica a cobrança e guarda o histórico para persistir em arquivo.
 */
public class SistemaCobrancaArquivo implements SistemaCobranca {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final List<String> registros = new ArrayList<>();

    @Override
    public void cobrar(Aluno aluno, Semestre semestre, List<Disciplina> disciplinas) {
        StringBuilder codigos = new StringBuilder();
        for (Disciplina disciplina : disciplinas) {
            if (codigos.length() > 0) {
                codigos.append(',');
            }
            codigos.append(disciplina.getCodigo());
        }
        registros.add(LocalDateTime.now().format(FORMATO)
                + "|" + aluno.getMatricula()
                + "|" + aluno.getNome()
                + "|" + semestre.getAno()
                + "|" + semestre.getPeriodo()
                + "|" + codigos);
    }

    public List<String> getRegistros() {
        return registros;
    }

    public void carregar(List<String> linhas) {
        registros.clear();
        registros.addAll(linhas);
    }
}
