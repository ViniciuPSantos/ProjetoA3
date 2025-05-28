package com.exemplo;
public class SessaoUsuario {
    private static SessaoUsuario instance;
    private Integer usuarioId;
    private String nomeUsuario;
    private String tipoUsuario; // 'bazar' ou 'cliente'
    private String emailUsuario; // Adicionando o campo para o email

    private SessaoUsuario() {}

    public static SessaoUsuario getInstance() {
        if (instance == null) {
            instance = new SessaoUsuario();
        }
        return instance;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public boolean isUsuarioLogado() {
        return usuarioId != null;
    }

    public void iniciarSessao(int id, String nome, String tipo, String email) {
        this.usuarioId = id;
        this.nomeUsuario = nome;
        this.tipoUsuario = tipo;
        this.emailUsuario = email; // Inicializando o email
    }

    public void encerrarSessao() {
        this.usuarioId = null;
        this.nomeUsuario = null;
        this.tipoUsuario = null;
        this.emailUsuario = null; // Limpando o email ao encerrar a sessão
    }
}
