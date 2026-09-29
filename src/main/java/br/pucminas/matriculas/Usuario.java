package br.pucminas.matriculas;

public abstract class Usuario {

    private String id;
    private String nome;
    private String login;
    private String senha;

    public Usuario(String id, String nome, String login, String senha) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
    }

    /**
     * HU01 — valida usuário e senha. O acesso só é liberado após autenticação.
     */
    public boolean autenticar(String login, String senha) {
        return login != null
                && senha != null
                && this.login.equalsIgnoreCase(login)
                && this.senha.equals(senha);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
