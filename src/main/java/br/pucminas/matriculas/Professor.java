package br.pucminas.matriculas;

import java.util.ArrayList;
import java.util.List;

public class Professor extends Usuario {

    private final List<Disciplina> disciplinas = new ArrayList<>();

    public Professor(String id, String nome, String login, String senha) {
        super(id, nome, login, senha);
    }

    /**
     * HU08 — alunos com matricula ativa na disciplina. Cancelados nao entram.
     */
    public List<Aluno> listarAlunos(Disciplina disciplina) {
        return null;
    }

    public List<Disciplina> getDisciplinas() {
        return disciplinas;
    }
}
