// TelaLogin.java
package com.exemplo;

import com.exemplo.DBConnector;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TelaLogin extends JFrame {

    private JTextField emailField;
    private JPasswordField senhaField;
    private JButton loginButton;
    private JButton cadastrarButton;
    private JCheckBox mostrarSenhaCheckBox;

    public TelaLogin() {
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 250);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Linha 0: Email
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        emailField = new JTextField(20);
        add(emailField, gbc);
        gbc.weightx = 0;

        // Linha 1: Senha
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Senha:"), gbc);

        gbc.gridx = 1;
        senhaField = new JPasswordField(20);
        add(senhaField, gbc);

        // Linha 2: Mostrar Senha CheckBox
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        mostrarSenhaCheckBox = new JCheckBox("Mostrar Senha");
        mostrarSenhaCheckBox.setFont(new Font("Arial", Font.PLAIN, 10));
        mostrarSenhaCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    senhaField.setEchoChar((char) 0);
                } else {
                    senhaField.setEchoChar((Character) UIManager.get("PasswordField.echoChar"));
                }
            }
        });
        add(mostrarSenhaCheckBox, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Linha 3: Botão Login
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginButton = new JButton("Login");
        add(loginButton, gbc);

        // Linha 4: Botão Cadastrar
        gbc.gridx = 0;
        gbc.gridy = 4;
        cadastrarButton = new JButton("Cadastrar");
        add(cadastrarButton, gbc);

        // 👈 Definir o botão de login como o botão padrão
        this.getRootPane().setDefaultButton(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText().trim();
                String senha = new String(senhaField.getPassword());
                String tipoUsuario = autenticarUsuario(email, senha);
                if (tipoUsuario != null) {
                    JOptionPane.showMessageDialog(TelaLogin.this, "Login realizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    TelaLogin.this.dispose();
                    if (tipoUsuario.equals("bazar")) {
                        SwingUtilities.invokeLater(() -> new TelaBazar().setVisible(true));
                    } else if (tipoUsuario.equals("cliente")) {
                        SwingUtilities.invokeLater(() -> new TelaCatalogo().setVisible(true));
                    }
                } else {
                    JOptionPane.showMessageDialog(TelaLogin.this, "Email ou senha incorretos.", "Erro de Login", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cadastrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> new TelaCadastro().setVisible(true));
            }
        });

        setVisible(true);
        // Opcional: dar foco inicial ao campo de email para melhor UX
        SwingUtilities.invokeLater(() -> emailField.requestFocusInWindow());
    }

    private String autenticarUsuario(String email, String senha) {
        // ... (lógica de autenticação com DBConnector e HASHING de senha) ...
        // Esta parte do código (usando DBConnector) permanece a mesma da refatoração anterior.
        // Lembre-se da importância de usar HASH de senhas aqui na comparação.
        String tipo = null;
        // ATENÇÃO: A senha no banco DEVE estar hasheada.
        // A comparação seria: BCrypt.checkpw(senhaDigitadaPeloUsuario, hashDoBanco)
        String sql = "SELECT id, nome, senha, tipo, email FROM usuarios WHERE email = ?";
        DBConnector dbConnector = new DBConnector();

        try (Connection conn = dbConnector.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String senhaBanco = rs.getString("senha"); // Idealmente, este é um HASH
                    // if (BCrypt.checkpw(senha, senhaBanco)) { // Exemplo com BCrypt
                    if (senha.equals(senhaBanco)) { // Mantendo sua lógica atual, mas insegura
                        tipo = rs.getString("tipo");
                        int userId = rs.getInt("id");
                        String nomeUsuario = rs.getString("nome");
                        String emailUsuario = rs.getString("email");
                        SessaoUsuario.getInstance().iniciarSessao(userId, nomeUsuario, tipo, emailUsuario);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao autenticar: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return tipo;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaLogin());
    }
}