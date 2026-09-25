package br.pucminas.matriculas;

import java.util.List;

/**
 * Ator externo notificado quando o aluno finaliza a matricula do semestre.
 */
public interface SistemaCobranca {

    void cobrar(Aluno aluno, Semestre semestre, List<Disciplina> disciplinas);
}
