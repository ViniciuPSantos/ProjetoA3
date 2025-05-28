package com.exemplo;

import com.exemplo.DBConnector; // 👈 Adicionado import do conector
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
// DriverManager não é mais necessário diretamente aqui para obter a conexão
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList; // 👈 Corrigido para import específico
import java.util.HashMap;
import java.util.List;    // 👈 Corrigido para import específico
import java.util.Map;

public class TelaSelecaoTamanho extends JFrame {

    private int produtoId;
    private String nomeProduto;
    private double valorProduto;
    private JComboBox<String> tamanhoComboBox;
    private JSpinner quantidadeSpinner;
    private JButton adicionarAoCarrinhoButton;
    private Map<String, String> tamanhoParaColuna = new HashMap<>();

    // 👇 Removidas as constantes de conexão daqui, pois o DBConnector cuidará disso
    // private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/projetoa3";
    // private static final String DB_USER = "root";
    // private static final String DB_PASSWORD = "";

    public TelaSelecaoTamanho(int produtoId, String nomeProduto, double valorProduto) {
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.valorProduto = valorProduto;

        tamanhoParaColuna.put("P", "quantidade_p");
        tamanhoParaColuna.put("M", "quantidade_m");
        tamanhoParaColuna.put("G", "quantidade_g");

        setTitle("Selecionar Tamanho e Quantidade");
        setSize(350, 200);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(new JLabel("Produto:"));
        add(new JLabel(nomeProduto));

        add(new JLabel("Tamanho:"));
        String[] tamanhosDisponiveis = obterTamanhosDisponiveis(produtoId);
        tamanhoComboBox = new JComboBox<>(tamanhosDisponiveis);
        add(tamanhoComboBox);

        add(new JLabel("Quantidade:"));
        SpinnerModel model = new SpinnerNumberModel(1, 1, 100, 1);
        quantidadeSpinner = new JSpinner(model);
        add(quantidadeSpinner);

        adicionarAoCarrinhoButton = new JButton("Adicionar ao Carrinho");
        adicionarAoCarrinhoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tamanhoSelecionado = (String) tamanhoComboBox.getSelectedItem();
                int quantidadeSelecionada = (int) quantidadeSpinner.getValue();

                SessaoUsuario sessao = SessaoUsuario.getInstance();
                if (sessao.isUsuarioLogado()) {
                    if (verificarEstoque(produtoId, tamanhoSelecionado, quantidadeSelecionada)) {
                        Carrinho.getInstance().adicionarItem(produtoId, nomeProduto, valorProduto, tamanhoSelecionado, quantidadeSelecionada);
                        JOptionPane.showMessageDialog(TelaSelecaoTamanho.this,
                                "Adicionado ao carrinho: " + quantidadeSelecionada + " x " + nomeProduto + " (" + tamanhoSelecionado + ")",
                                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        TelaSelecaoTamanho.this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(TelaSelecaoTamanho.this,
                                "Estoque insuficiente para " + nomeProduto + " (" + tamanhoSelecionado + ") na quantidade desejada.",
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(TelaSelecaoTamanho.this,
                            "Você precisa estar logado para adicionar itens ao carrinho.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    TelaLogin telaLogin = new TelaLogin();
                    telaLogin.setVisible(true);
                }
            }
        });
        add(new JLabel("")); // Espaço em branco
        add(adicionarAoCarrinhoButton);

        setVisible(true);
    }

    private String[] obterTamanhosDisponiveis(int produtoId) {
        List<String> tamanhos = new ArrayList<>(); // 👈 Usando List<String> diretamente
        String sql = "SELECT quantidade_p, quantidade_m, quantidade_g FROM produtos WHERE id = ?";
        DBConnector dbConnector = new DBConnector(); // 👈 Instanciando seu conector

        // Usando try-with-resources para Connection e PreparedStatement
        try (Connection conn = dbConnector.conectar(); // 👈 Usando o método conectar()
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, produtoId);
            try (ResultSet rs = pstmt.executeQuery()) { // 👈 ResultSet também no try-with-resources
                if (rs.next()) {
                    if (rs.getInt("quantidade_p") > 0) tamanhos.add("P");
                    if (rs.getInt("quantidade_m") > 0) tamanhos.add("M");
                    if (rs.getInt("quantidade_g") > 0) tamanhos.add("G");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao obter tamanhos disponíveis: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return tamanhos.toArray(new String[0]);
    }

    private boolean verificarEstoque(int produtoId, String tamanho, int quantidade) {
        String colunaEstoque = tamanhoParaColuna.get(tamanho);
        if (colunaEstoque == null) {
            return false; // Tamanho inválido
        }
        String sql = "SELECT " + colunaEstoque + " FROM produtos WHERE id = ?";
        DBConnector dbConnector = new DBConnector(); // 👈 Instanciando seu conector

        // Usando try-with-resources para Connection e PreparedStatement
        try (Connection conn = dbConnector.conectar(); // 👈 Usando o método conectar()
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, produtoId);
            try (ResultSet rs = pstmt.executeQuery()) { // 👈 ResultSet também no try-with-resources
                if (rs.next()) {
                    int estoqueDisponivel = rs.getInt(colunaEstoque);
                    return estoqueDisponivel >= quantidade;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao verificar estoque: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return false;
    }
}