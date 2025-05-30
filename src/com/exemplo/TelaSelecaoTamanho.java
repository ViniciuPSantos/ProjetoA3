package com.exemplo; // Ou o seu pacote correto, ex: Site

import com.exemplo.DBConnector; // Certifique-se que o DBConnector está acessível
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
// O Map tamanhoParaColuna não é mais necessário
// import java.util.Map;
// import java.util.HashMap;

public class TelaSelecaoTamanho extends JFrame {

    private int produtoId;
    private String nomeProduto;
    private double valorProduto;
    private JComboBox<String> tamanhoComboBox;
    private JSpinner quantidadeSpinner;
    private JButton adicionarAoCarrinhoButton;
    // private Map<String, String> tamanhoParaColuna = new HashMap<>(); // 👈 REMOVIDO

    public TelaSelecaoTamanho(int produtoId, String nomeProduto, double valorProduto) {
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.valorProduto = valorProduto;

        // tamanhoParaColuna.put("P", "quantidade_p"); // 👈 REMOVIDO
        // tamanhoParaColuna.put("M", "quantidade_m"); // 👈 REMOVIDO
        // tamanhoParaColuna.put("G", "quantidade_g"); // 👈 REMOVIDO

        setTitle("Selecionar Tamanho e Quantidade para: " + nomeProduto);
        setSize(400, 220); // Ajuste de tamanho se necessário
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10,10,10,10));


        add(new JLabel("Produto:"));
        JLabel nomeProdutoLabel = new JLabel(nomeProduto);
        nomeProdutoLabel.setFont(new Font(nomeProdutoLabel.getFont().getName(), Font.BOLD, nomeProdutoLabel.getFont().getSize()));
        add(nomeProdutoLabel);

        add(new JLabel("Tamanho Disponível:"));
        // Este método agora busca da tabela estoque_variacoes
        String[] tamanhosDisponiveis = obterTamanhosDisponiveis(produtoId);
        if (tamanhosDisponiveis.length == 0) {
            // Tratar caso onde não há tamanhos com estoque
            tamanhoComboBox = new JComboBox<>(new String[]{"Sem estoque"});
            tamanhoComboBox.setEnabled(false);
            // Considerar desabilitar outros campos também
        } else {
            tamanhoComboBox = new JComboBox<>(tamanhosDisponiveis);
        }
        add(tamanhoComboBox);

        add(new JLabel("Quantidade:"));
        // Definir um máximo para o spinner baseado no estoque do primeiro tamanho? (mais complexo)
        // Por agora, um máximo genérico. A verificação de estoque real ocorre ao adicionar.
        SpinnerModel model = new SpinnerNumberModel(1, 1, 100, 1);
        quantidadeSpinner = new JSpinner(model);
        add(quantidadeSpinner);

        adicionarAoCarrinhoButton = new JButton("Adicionar ao Carrinho");
        // Desabilitar botão se não houver tamanhos/estoque
        if (tamanhosDisponiveis.length == 0) {
            adicionarAoCarrinhoButton.setEnabled(false);
        }

        adicionarAoCarrinhoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tamanhoSelecionado = (String) tamanhoComboBox.getSelectedItem();
                if (tamanhoSelecionado == null || "Sem estoque".equals(tamanhoSelecionado)) {
                    JOptionPane.showMessageDialog(TelaSelecaoTamanho.this,
                            "Nenhum tamanho selecionável ou produto sem estoque.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int quantidadeSelecionada = (int) quantidadeSpinner.getValue();

                SessaoUsuario sessao = SessaoUsuario.getInstance(); // Supondo que esta classe exista
                if (sessao.isUsuarioLogado()) {
                    // Este método agora verifica na tabela estoque_variacoes
                    if (verificarEstoque(produtoId, tamanhoSelecionado, quantidadeSelecionada)) {
                        // Supondo que Carrinho.getInstance() exista e funcione
                        Carrinho.getInstance().adicionarItem(produtoId, nomeProduto, valorProduto, tamanhoSelecionado, quantidadeSelecionada);
                        JOptionPane.showMessageDialog(TelaSelecaoTamanho.this,
                                "Adicionado ao carrinho: " + quantidadeSelecionada + " x " + nomeProduto + " (" + tamanhoSelecionado + ")",
                                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        TelaSelecaoTamanho.this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(TelaSelecaoTamanho.this,
                                "Estoque insuficiente para " + nomeProduto + " (Tamanho: " + tamanhoSelecionado + ") na quantidade desejada.",
                                "Estoque Insuficiente", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(TelaSelecaoTamanho.this,
                            "Você precisa estar logado para adicionar itens ao carrinho.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    // new TelaLogin().setVisible(true); // Supondo que TelaLogin exista
                }
            }
        });
        add(new JLabel("")); // Espaço em branco
        add(adicionarAoCarrinhoButton);

        setVisible(true);
    }

    private String[] obterTamanhosDisponiveis(int produtoId) {
        List<String> tamanhos = new ArrayList<>();
        // 👇 SQL MODIFICADO para buscar da tabela estoque_variacoes
        //    e buscar o tipo_produto para ordenar ou filtrar tamanhos
        String sql = "SELECT ev.tamanho_descricao " +
                     "FROM estoque_variacoes ev " +
                     "JOIN produtos p ON ev.produto_id = p.id " +
                     "WHERE ev.produto_id = ? AND ev.quantidade > 0 " +
                     "ORDER BY " +
                     "  CASE p.tipo_produto " +
                     "    WHEN 'ROUPA' THEN " +
                     "      CASE ev.tamanho_descricao " +
                     "        WHEN 'P' THEN 1 " +
                     "        WHEN 'M' THEN 2 " +
                     "        WHEN 'G' THEN 3 " +
                     "        ELSE 4 END " + // Para outros tamanhos de roupa se houver
                     "    WHEN 'TENIS' THEN CAST(REGEXP_REPLACE(ev.tamanho_descricao, '[^0-9]', '') AS UNSIGNED) " + // Ordena numericamente para tênis
                     "    ELSE 99 END, ev.tamanho_descricao"; // Fallback sort

        DBConnector dbConnector = new DBConnector();

        try (Connection conn = dbConnector.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, produtoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tamanhos.add(rs.getString("tamanho_descricao"));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao obter tamanhos disponíveis: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Importante para debug
        }
        return tamanhos.toArray(new String[0]);
    }

    private boolean verificarEstoque(int produtoId, String tamanhoDescricao, int quantidadeDesejada) {
        // O parâmetro 'tamanhoDescricao' agora é o valor direto (ex: "P", "M", "38")
        if (tamanhoDescricao == null || tamanhoDescricao.isEmpty()) {
            return false;
        }
        // 👇 SQL MODIFICADO para buscar da tabela estoque_variacoes
        String sql = "SELECT quantidade FROM estoque_variacoes WHERE produto_id = ? AND tamanho_descricao = ?";
        DBConnector dbConnector = new DBConnector();

        try (Connection conn = dbConnector.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, produtoId);
            pstmt.setString(2, tamanhoDescricao); // Usa o tamanho_descricao diretamente

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int estoqueDisponivel = rs.getInt("quantidade");
                    return estoqueDisponivel >= quantidadeDesejada;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao verificar estoque: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Importante para debug
        }
        return false; // Retorna false se o produto/tamanho não for encontrado ou se houver erro
    }

    // Exemplo de main para testar a tela independentemente (opcional)
    // public static void main(String[] args) {
    //     // Você precisaria de uma forma de simular a SessaoUsuario e Carrinho se não estiverem
    //     // implementadas ou se dependerem de outras partes não disponíveis aqui.
    //     // E também um produtoId, nome, valor válidos do seu banco.
    //     // Exemplo:
    //     // SessaoUsuario.getInstance().iniciarSessao(1, "Teste", "cliente", "teste@teste.com");
    //     SwingUtilities.invokeLater(() -> new TelaSelecaoTamanho(1, "Camiseta Exemplo", 29.99));
    // }
}