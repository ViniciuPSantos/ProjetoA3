package com.exemplo;

// Removido DBConnector daqui, já que não é usado diretamente no escopo global da classe
// import com.exemplo.DBConnector; // Será usado dentro do método autenticarUsuario
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class TelaLogin extends JFrame {

    private JTextField emailField;
    private JPasswordField senhaField;
    private JButton loginButton;
    private JButton cadastrarButton;
    private JCheckBox mostrarSenhaCheckBox;

    // --- Início das Modificações para Bloqueio de Login ---
    private static Map<String, Integer> tentativasLogin = new HashMap<>();
    private static Map<String, LocalDateTime> usuariosBloqueados = new HashMap<>();
    private static final int MAX_TENTATIVAS = 3;
    private static final int MINUTOS_BLOQUEIO = 5;
    // --- Fim das Modificações para Bloqueio de Login ---

    public TelaLogin() {
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 280); // Aumentei um pouco a altura para acomodar possíveis mensagens
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

        this.getRootPane().setDefaultButton(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText().trim().toLowerCase(); // Padronizar para minúsculas
                String senha = new String(senhaField.getPassword());

                // --- Início da Lógica de Bloqueio ---
                if (estaBloqueado(email)) {
                    LocalDateTime tempoBloqueio = usuariosBloqueados.get(email);
                    LocalDateTime tempoDesbloqueio = tempoBloqueio.plusMinutes(MINUTOS_BLOQUEIO);
                    long minutosRestantes = ChronoUnit.MINUTES.between(LocalDateTime.now(), tempoDesbloqueio);
                    long segundosRestantes = ChronoUnit.SECONDS.between(LocalDateTime.now(), tempoDesbloqueio) % 60;

                    if (minutosRestantes < 0) minutosRestantes = 0; // Garante que não mostre tempo negativo
                    if (segundosRestantes < 0) segundosRestantes = 0;


                    JOptionPane.showMessageDialog(TelaLogin.this,
                            "Conta bloqueada devido a múltiplas tentativas falhas.\n" +
                                    "Tente novamente em aproximadamente " + (minutosRestantes + (segundosRestantes > 0 ? 1 : 0) ) + " minuto(s).",
                            "Conta Bloqueada", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // --- Fim da Lógica de Bloqueio ---

                String tipoUsuario = autenticarUsuario(email, senha);

                if (tipoUsuario != null) {
                    limparTentativas(email); // Limpa tentativas após login bem-sucedido
                    JOptionPane.showMessageDialog(TelaLogin.this, "Login realizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    SessaoUsuario.getInstance().iniciarSessao(SessaoUsuario.getInstance().getUsuarioId(), SessaoUsuario.getInstance().getNomeUsuario(), tipoUsuario, email); // Re-iniciar sessão com tipo correto se necessário
                    TelaLogin.this.dispose();
                    if (tipoUsuario.equals("bazar")) {
                        SwingUtilities.invokeLater(() -> new TelaBazar().setVisible(true));
                    } else if (tipoUsuario.equals("cliente")) {
                        SwingUtilities.invokeLater(() -> new TelaCatalogo().setVisible(true));
                    }
                } else {
                    registrarTentativaFalha(email);
                    int tentativasRestantes = MAX_TENTATIVAS - tentativasLogin.getOrDefault(email, 0);

                    if (tentativasLogin.getOrDefault(email, 0) >= MAX_TENTATIVAS) {
                        bloquearUsuario(email);
                        JOptionPane.showMessageDialog(TelaLogin.this,
                                "Email ou senha incorretos. Você excedeu o número máximo de tentativas.\n" +
                                        "Sua conta foi bloqueada por " + MINUTOS_BLOQUEIO + " minutos.",
                                "Erro de Login", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(TelaLogin.this,
                                "Email ou senha incorretos.\n" +
                                (tentativasRestantes > 0 ? "Tentativas restantes: " + tentativasRestantes : "Última tentativa!"),
                                "Erro de Login", JOptionPane.ERROR_MESSAGE);
                    }
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
        SwingUtilities.invokeLater(() -> emailField.requestFocusInWindow());
    }

    // --- Início dos Métodos Auxiliares para Bloqueio de Login ---
    private void registrarTentativaFalha(String email) {
        tentativasLogin.put(email, tentativasLogin.getOrDefault(email, 0) + 1);
    }

    private void limparTentativas(String email) {
        tentativasLogin.remove(email);
        usuariosBloqueados.remove(email); // Também remove da lista de bloqueados
    }

    private void bloquearUsuario(String email) {
        usuariosBloqueados.put(email, LocalDateTime.now());
        // Não removemos as tentativas daqui, pois o método estaBloqueado pode precisar delas
        // ou podemos simplesmente confiar no tempo de bloqueio.
        // Para simplificar, o desbloqueio via tempo já limpa as tentativas em `estaBloqueado`.
    }

    private boolean estaBloqueado(String email) {
        if (usuariosBloqueados.containsKey(email)) {
            LocalDateTime tempoBloqueio = usuariosBloqueados.get(email);
            LocalDateTime agora = LocalDateTime.now();

            if (tempoBloqueio.plusMinutes(MINUTOS_BLOQUEIO).isAfter(agora)) {
                return true; // Ainda está bloqueado
            } else {
                // Tempo de bloqueio expirou, remove o usuário da lista de bloqueados
                limparTentativas(email); // Limpa tentativas e registro de bloqueio
                return false; // Não está mais bloqueado
            }
        }
        return false; // Não está na lista de bloqueados
    }
    // --- Fim dos Métodos Auxiliares para Bloqueio de Login ---

    private String autenticarUsuario(String email, String senha) {
        String tipo = null;
        // ATENÇÃO: A senha no banco DEVE estar hasheada.
        // A comparação seria: BCrypt.checkpw(senhaDigitadaPeloUsuario, hashDoBanco)
        String sql = "SELECT id, nome, senha, tipo, email FROM usuarios WHERE email = ?"; // Adicionado 'email' ao SELECT para a SessaoUsuario
        DBConnector dbConnector = new DBConnector(); // Instancia o DBConnector aqui

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
                        String emailUsuario = rs.getString("email"); // Obtém o email do banco
                        // Iniciar a sessão aqui se as credenciais estiverem corretas
                        SessaoUsuario.getInstance().iniciarSessao(userId, nomeUsuario, tipo, emailUsuario);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao autenticar: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Importante para debug no console
        }
        return tipo; // Retorna null se a autenticação falhar (email não encontrado ou senha incorreta)
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaLogin());
    }
}