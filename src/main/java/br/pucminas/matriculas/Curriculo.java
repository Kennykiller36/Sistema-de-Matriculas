package br.pucminas.matriculas;

import java.util.ArrayList;
import java.util.List;

public class Curriculo {

    private Semestre semestre;
    private final List<Disciplina> disciplinas = new ArrayList<>();

    public Curriculo(Semestre semestre) {
        this.semestre = semestre;
    }

    public void adicionarDisciplina(Disciplina disciplina) {
    }

    public Semestre getSemestre() {
        return semestre;
    }

    public void setSemestre(Semestre semestre) {
        this.semestre = semestre;
    }

    public List<Disciplina> getDisciplinas() {
        return disciplinas;
    }
}
