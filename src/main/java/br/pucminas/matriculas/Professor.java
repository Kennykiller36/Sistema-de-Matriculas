package br.pucminas.matriculas;

import java.util.ArrayList;
import java.util.List;

public class Professor extends Usuario {

    private final List<Disciplina> disciplinas = new ArrayList<>();

    public Professor(String id, String nome, String login, String senha) {
        super(id, nome, login, senha);
    }

    /**
     * HU08 — alunos com matrícula ativa na disciplina. Cancelados não entram.
     */
    public List<Aluno> listarAlunos(Disciplina disciplina) {
        if (disciplina == null || !disciplinas.contains(disciplina)) {
            throw new RegraNegocioException("Disciplina não vinculada a este professor.");
        }
        List<Aluno> alunos = new ArrayList<>();
        for (Matricula matricula : disciplina.getMatriculas()) {
            if (matricula.isAtiva() && !alunos.contains(matricula.getAluno())) {
                alunos.add(matricula.getAluno());
            }
        }
        return alunos;
    }

    public List<Disciplina> getDisciplinas() {
        return disciplinas;
    }
}
