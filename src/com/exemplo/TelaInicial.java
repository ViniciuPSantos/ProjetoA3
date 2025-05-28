package com.exemplo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TelaInicial extends JFrame {

    public TelaInicial() {
        setTitle("Bazar Online - Tela Inicial");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500); // Um tamanho inicial
        setLocationRelativeTo(null); // Centraliza a janela

        JPanel painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BorderLayout());

        // Adicionar componentes aqui
        JLabel tituloLabel = new JLabel("Nome do Bazar", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JTextArea descricaoTextArea = new JTextArea(
                "Transforme seu guarda-roupa e o mundo com o nosso Bazar Solidário!\n\n" +
                "Em um momento de reflexão global, convidamos você a fazer parte de uma revolução na\n" +
                "moda. Nosso Bazar Solidário é mais que uma troca de roupas: é um movimento em\n" +
                "direção a um futuro mais verde e justo.\n\n" +
                "Descubra a moda ecológica: valorizamos a beleza da reutilização, a força da doação e a\n" +
                "urgência da sustentabilidade. Dê um novo lar àquela peça especial e encontre tesouros\n" +
                "únicos, tudo enquanto contribui para um planeta mais saudável e uma comunidade mais\n" +
                "forte.\n\n" +
                "Participe da economia circular: aqui, seus itens ganham nova vida, evitando o desperdício\n" +
                "e inspirando um consumo consciente.\n\n" +
                "Junte-se a nós: seja parte da mudança, adote um estilo com propósito e mostre que a\n" +
                "moda pode ser uma poderosa ferramenta de transformação social e ambiental.\n\n" +
                "Bazar Solidário: Vista essa ideia!");
        descricaoTextArea.setEditable(false);
        descricaoTextArea.setLineWrap(true);
        descricaoTextArea.setWrapStyleWord(true);

        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        JButton loginButton = new JButton("Login");
        JButton cadastrarButton = new JButton("Cadastrar-se");
        JButton navegarButton = new JButton("Navegar como visitante");

        // Adicionando ActionListener ao botão "Login"
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cria e exibe a TelaLogin
                TelaLogin telaDeLogin = new TelaLogin();
                telaDeLogin.setVisible(true);
            }
        });

        // Adicionando ActionListener ao botão "Cadastrar-se"
        cadastrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cria e exibe a TelaCadastro (corrigido o nome da classe)
                TelaCadastro telaDeCadastro = new TelaCadastro();
                telaDeCadastro.setVisible(true);
            }
        });

        // Adicionando ActionListener ao botão "Navegar sem login"
        navegarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cria e exibe a TelaCatalogo
                TelaCatalogo telaDeCatalogo = new TelaCatalogo();
                telaDeCatalogo.setVisible(true);
            }
        });

        botoesPanel.add(loginButton);
        botoesPanel.add(cadastrarButton);
        botoesPanel.add(navegarButton);

        painelPrincipal.add(tituloLabel, BorderLayout.NORTH);
        painelPrincipal.add(new JScrollPane(descricaoTextArea), BorderLayout.CENTER);
        painelPrincipal.add(botoesPanel, BorderLayout.SOUTH);

        add(painelPrincipal);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaInicial());
    }
}
