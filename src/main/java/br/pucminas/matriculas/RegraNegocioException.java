package br.pucminas.matriculas;

/**
 * Regra de negócio do sistema de matrículas violada.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
