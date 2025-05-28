package com.exemplo;
import com.exemplo.DBConnector; // 👈 Adicionado import do seu conector
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
// DriverManager não é mais necessário diretamente aqui para obter a conexão
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class TelaCarrinho extends JFrame {

    private JList<ItemCarrinho> listaItens;
    private DefaultListModel<ItemCarrinho> listModel;
    private JButton removerItemButton;
    private JButton alterarQtdButton;
    private JLabel totalLabel;
    private JTextArea enderecoTextArea;
    private JRadioButton pixRadioButton;
    private JRadioButton creditoRadioButton;
    private JButton confirmarCompraButton;
    // private int usuarioIdCliente = SessaoUsuario.getInstance().getUsuarioId() != null ? SessaoUsuario.getInstance().getUsuarioId() : -1; // Valor padrão -1 se não logado

    // 👇 Removidas as constantes de conexão daqui
    // private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/projetoa3";
    // private static final String DB_USER = "root";
    // private static final String DB_PASSWORD = "";

    public TelaCarrinho() {
        setTitle("Carrinho de Compras");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Verifica se o usuário está logado ao iniciar a tela do carrinho
        if (!SessaoUsuario.getInstance().isUsuarioLogado()) {
            JOptionPane.showMessageDialog(this,
                    "Você precisa estar logado para acessar o carrinho.",
                    "Acesso Negado",
                    JOptionPane.ERROR_MESSAGE);
            // Fecha a tela do carrinho se o usuário não estiver logado
            // É importante chamar dispose no Event Dispatch Thread
            SwingUtilities.invokeLater(this::dispose);
            return; // Impede a continuação da construção da UI
        }


        listModel = new DefaultListModel<>();
        listaItens = new JList<>(listModel);
        listaItens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaItens.setCellRenderer(new ItemCarrinhoRenderer()); // Para melhor exibição

        JPanel listaPanel = new JPanel(new BorderLayout());
        listaPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        listaPanel.add(new JScrollPane(listaItens), BorderLayout.CENTER);

        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        removerItemButton = new JButton("Remover Item");
        alterarQtdButton = new JButton("Alterar Quantidade");
        botoesPanel.add(removerItemButton);
        botoesPanel.add(alterarQtdButton);
        listaPanel.add(botoesPanel, BorderLayout.SOUTH);

        JPanel checkoutPanel = new JPanel();
        checkoutPanel.setLayout(new BoxLayout(checkoutPanel, BoxLayout.Y_AXIS));
        checkoutPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        totalLabel = new JLabel("Total: R$ 0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        enderecoTextArea = new JTextArea(5, 20);
        JScrollPane enderecoScrollPane = new JScrollPane(enderecoTextArea);
        enderecoScrollPane.setBorder(BorderFactory.createTitledBorder("Endereço de Entrega:"));
        enderecoScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);


        JPanel pagamentoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pagamentoPanel.setBorder(BorderFactory.createTitledBorder("Forma de Pagamento"));
        pixRadioButton = new JRadioButton("Pix");
        creditoRadioButton = new JRadioButton("Cartão de Crédito");
        ButtonGroup pagamentoGroup = new ButtonGroup();
        pagamentoGroup.add(pixRadioButton);
        pagamentoGroup.add(creditoRadioButton);
        pixRadioButton.setSelected(true);
        pagamentoPanel.add(pixRadioButton);
        pagamentoPanel.add(creditoRadioButton);
        pagamentoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        confirmarCompraButton = new JButton("Confirmar Compra");
        confirmarCompraButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmarCompraButton.addActionListener(e -> {
            // Obter o usuarioId no momento da ação, garantindo que é o mais atual
            Integer usuarioIdAtual = SessaoUsuario.getInstance().getUsuarioId();
            if (usuarioIdAtual == null) {
                JOptionPane.showMessageDialog(TelaCarrinho.this, "Sessão expirada ou usuário não logado. Por favor, faça login novamente.", "Erro de Sessão", JOptionPane.ERROR_MESSAGE);
                // Opcional: fechar carrinho e/ou abrir tela de login
                // dispose();
                // new TelaLogin().setVisible(true);
                return;
            }

            String endereco = enderecoTextArea.getText().trim();
            String formaPagamento = pixRadioButton.isSelected() ? "Pix" : "Cartão de Crédito";
            double totalCompra = calcularTotalCarrinhoSingleton();

            if (endereco.isEmpty()) {
                JOptionPane.showMessageDialog(TelaCarrinho.this, "Por favor, preencha o endereço de entrega.", "Endereço Obrigatório", JOptionPane.WARNING_MESSAGE);
                enderecoTextArea.requestFocusInWindow();
                return;
            }

            if (Carrinho.getInstance().getItens().isEmpty()) {
                JOptionPane.showMessageDialog(TelaCarrinho.this, "O carrinho está vazio. Adicione itens antes de confirmar a compra.", "Carrinho Vazio", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int pedidoId = salvarPedido(usuarioIdAtual, endereco, formaPagamento, totalCompra, obterEmailUsuarioLogado());

            if (pedidoId > 0) {
                boolean itensSalvos = salvarItensPedido(pedidoId, Carrinho.getInstance().getItens());
                if (itensSalvos) {
                    JOptionPane.showMessageDialog(TelaCarrinho.this,
                            "Compra confirmada com sucesso!\nNúmero do Pedido: " + pedidoId +
                                    "\nTotal: R$" + String.format("%.2f", totalCompra) +
                                    "\nPagamento: " + formaPagamento,
                            "Confirmação de Compra", JOptionPane.INFORMATION_MESSAGE);

                    Carrinho.getInstance().limparCarrinho();
                    atualizarListaItens();
                    atualizarTotalLabel();

                    JOptionPane.showMessageDialog(TelaCarrinho.this, "Obrigado pela sua compra!\nVolte sempre!", "Boa Compra!", JOptionPane.INFORMATION_MESSAGE);
                    // Decide se quer fechar a aplicação inteira ou apenas as telas relacionadas à compra
                    // System.exit(0); // Encerra toda a aplicação
                    dispose(); // Fecha a tela do carrinho
                    // Potencialmente fechar catálogo também ou redirecionar para uma tela principal
                } else {
                    JOptionPane.showMessageDialog(TelaCarrinho.this, "Erro ao salvar os itens do pedido. O pedido foi registrado com ID " + pedidoId + " mas os itens falharam. Contate o suporte.", "Erro Crítico", JOptionPane.ERROR_MESSAGE);
                    // Aqui seria importante ter uma lógica para lidar com essa inconsistência (ex: marcar pedido como pendente de revisão)
                }
            } else {
                JOptionPane.showMessageDialog(TelaCarrinho.this, "Erro ao registrar o pedido. A compra não foi finalizada.", "Erro na Compra", JOptionPane.ERROR_MESSAGE);
            }
        });

        checkoutPanel.add(totalLabel);
        checkoutPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        checkoutPanel.add(enderecoScrollPane);
        checkoutPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        checkoutPanel.add(pagamentoPanel);
        checkoutPanel.add(Box.createVerticalStrut(20)); // Mais espaço antes do botão
        checkoutPanel.add(confirmarCompraButton);
        checkoutPanel.add(Box.createVerticalGlue()); // Empurra o botão para cima se houver espaço

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(checkoutPanel, BorderLayout.CENTER); // checkoutPanel pode usar mais espaço

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listaPanel, rightPanel);
        splitPane.setDividerLocation(450); // Ajuste o divisor conforme necessário
        splitPane.setResizeWeight(0.5); // Distribui o espaço de forma mais equilibrada

        add(splitPane, BorderLayout.CENTER);

        removerItemButton.addActionListener(e -> {
            int selectedIndex = listaItens.getSelectedIndex();
            if (selectedIndex != -1) {
                ItemCarrinho removedItem = listModel.getElementAt(selectedIndex);
                Carrinho.getInstance().removerItem(removedItem.getProdutoId(), removedItem.getTamanho());
                atualizarListaItens();
                atualizarTotalLabel();
            } else {
                JOptionPane.showMessageDialog(TelaCarrinho.this, "Selecione um item para remover.", "Nenhum Item Selecionado", JOptionPane.WARNING_MESSAGE);
            }
        });

        alterarQtdButton.addActionListener(e -> {
            int selectedIndex = listaItens.getSelectedIndex();
            if (selectedIndex != -1) {
                ItemCarrinho itemParaAlterar = listModel.getElementAt(selectedIndex);
                String novoValorStr = JOptionPane.showInputDialog(TelaCarrinho.this,
                        "Digite a nova quantidade para " + itemParaAlterar.getNomeProduto() + " ("+ itemParaAlterar.getTamanho() +"):",
                        itemParaAlterar.getQuantidade());
                if (novoValorStr != null) {
                    try {
                        int novaQuantidade = Integer.parseInt(novoValorStr);
                        if (novaQuantidade > 0) {
                            // Verificar estoque antes de alterar
                            if (verificarEstoqueDisponivel(itemParaAlterar.getProdutoId(), itemParaAlterar.getTamanho(), novaQuantidade)) {
                                itemParaAlterar.setQuantidade(novaQuantidade); // Atualiza no objeto do carrinho
                                Carrinho.getInstance().atualizarItem(itemParaAlterar); // Atualiza no singleton Carrinho
                                atualizarListaItens();
                                atualizarTotalLabel();
                            } else {
                                JOptionPane.showMessageDialog(TelaCarrinho.this, "Estoque insuficiente para a quantidade desejada.", "Estoque Insuficiente", JOptionPane.WARNING_MESSAGE);
                            }
                        } else if (novaQuantidade == 0) {
                            // Remover item se a quantidade for 0
                            Carrinho.getInstance().removerItem(itemParaAlterar.getProdutoId(), itemParaAlterar.getTamanho());
                            atualizarListaItens();
                            atualizarTotalLabel();
                        }
                        else {
                            JOptionPane.showMessageDialog(TelaCarrinho.this, "A quantidade deve ser um número positivo.", "Quantidade Inválida", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(TelaCarrinho.this, "Por favor, digite um número válido para a quantidade.", "Entrada Inválida", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(TelaCarrinho.this, "Selecione um item para alterar a quantidade.", "Nenhum Item Selecionado", JOptionPane.WARNING_MESSAGE);
            }
        });

        atualizarListaItens();
        atualizarTotalLabel();

        setVisible(true);
    }

    private boolean verificarEstoqueDisponivel(int produtoId, String tamanho, int quantidadeDesejada) {
        DBConnector dbConnector = new DBConnector();
        String colunaEstoque = "";
        switch (tamanho.toUpperCase()) {
            case "P": colunaEstoque = "quantidade_p"; break;
            case "M": colunaEstoque = "quantidade_m"; break;
            case "G": colunaEstoque = "quantidade_g"; break;
            default: return false; // Tamanho inválido
        }
        String sql = "SELECT " + colunaEstoque + " FROM produtos WHERE id = ?";
        try (Connection conn = dbConnector.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, produtoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(colunaEstoque) >= quantidadeDesejada;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao verificar estoque: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }


    private void atualizarListaItens() {
        listModel.clear();
        for (ItemCarrinho item : Carrinho.getInstance().getItens()) {
            listModel.addElement(item);
        }
    }

    private double calcularTotalCarrinhoSingleton() {
        return Carrinho.getInstance().getTotal();
    }

    private void atualizarTotalLabel() {
        totalLabel.setText("Total: R$ " + String.format("%.2f", calcularTotalCarrinhoSingleton()));
    }

    private String obterEmailUsuarioLogado() {
        return SessaoUsuario.getInstance().getEmailUsuario();
    }

    private int salvarPedido(int usuarioId, String endereco, String formaPagamento, double total, String email) {
        int pedidoId = -1;
        String sql = "INSERT INTO pedidos (usuario_id, data_pedido, endereco_entrega, forma_pagamento, total, email, status) VALUES (?, NOW(), ?, ?, ?, ?, ?)";
        DBConnector dbConnector = new DBConnector(); // 👈 Instanciando seu conector

        // Usando try-with-resources
        try (Connection conn = dbConnector.conectar(); // 👈 Usando o método conectar()
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, usuarioId);
            pstmt.setString(2, endereco);
            pstmt.setString(3, formaPagamento);
            pstmt.setDouble(4, total);
            pstmt.setString(5, email);
            pstmt.setString(6, "Processando"); // Status inicial do pedido

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) { // ResultSet também no try-with-resources
                    if (generatedKeys.next()) {
                        pedidoId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar pedido: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return pedidoId;
    }

    private boolean salvarItensPedido(int pedidoId, List<ItemCarrinho> itens) {
        boolean sucessoGeral = true;
        String sqlInsertItem = "INSERT INTO itens_pedido (pedido_id, produto_id, quantidade, preco_unitario, subtotal, tamanho) VALUES (?, ?, ?, ?, ?, ?)";
        DBConnector dbConnector = new DBConnector(); // 👈 Instanciando seu conector

        // A transação deve ser gerenciada aqui para todas as operações (insert de itens e update de estoque)
        try (Connection conn = dbConnector.conectar()) { // 👈 Usando o método conectar()
            conn.setAutoCommit(false); // Inicia a transação

            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsertItem)) {
                for (ItemCarrinho item : itens) {
                    pstmtInsert.setInt(1, pedidoId);
                    pstmtInsert.setInt(2, item.getProdutoId());
                    pstmtInsert.setInt(3, item.getQuantidade());
                    pstmtInsert.setDouble(4, item.getValorUnitario()); // Certifique-se que ItemCarrinho tem getValorUnitario()
                    pstmtInsert.setDouble(5, item.getSubtotal());    // Certifique-se que ItemCarrinho tem getSubtotal()
                    pstmtInsert.setString(6, item.getTamanho());
                    pstmtInsert.addBatch();
                }
                int[] results = pstmtInsert.executeBatch();
                for (int result : results) {
                    if (result <= 0 && result != Statement.SUCCESS_NO_INFO) { // SUCCESS_NO_INFO é normal para alguns drivers/configurações
                        sucessoGeral = false;
                        break;
                    }
                }
            } // pstmtInsert é fechado aqui

            if (sucessoGeral) {
                // Atualizar o estoque DENTRO da mesma transação
                for (ItemCarrinho item : itens) {
                    atualizarEstoque(conn, item.getProdutoId(), item.getTamanho(), -item.getQuantidade()); // Passa a conexão da transação
                }
            }

            if (sucessoGeral) {
                conn.commit(); // Confirma a transação se tudo deu certo
            } else {
                conn.rollback(); // Desfaz a transação se algo deu errado
                JOptionPane.showMessageDialog(this, "Falha ao salvar um ou mais itens do pedido ou atualizar estoque. A transação foi desfeita.", "Erro na Transação", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro crítico ao salvar itens do pedido ou gerenciar transação: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            sucessoGeral = false;
            // Não há como garantir rollback aqui se a conexão falhou antes de conn.rollback() ser chamado,
            // mas o try-with-resources fechará a conexão. O banco pode ter desfeito a transação se a conexão caiu.
        }
        return sucessoGeral;
    }


    // atualizarEstoque já recebe a Connection, o que é bom para transações.
    // Ele será chamado DENTRO da transação de salvarItensPedido.
    private void atualizarEstoque(Connection conn, int produtoId, String tamanho, int quantidadeAlteracao) throws SQLException {
        String colunaQuantidade = "";
        switch (tamanho.toUpperCase()) {
            case "P": colunaQuantidade = "quantidade_p"; break;
            case "M": colunaQuantidade = "quantidade_m"; break;
            case "G": colunaQuantidade = "quantidade_g"; break;
            default:
                throw new SQLException("Tamanho inválido para atualização de estoque: " + tamanho); // Lança exceção para rollback
        }
        String sql = "UPDATE produtos SET " + colunaQuantidade + " = " + colunaQuantidade + " + ? WHERE id = ?";
        // Usando try-with-resources para o PreparedStatement, mas não para a Connection, pois ela é gerenciada externamente.
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantidadeAlteracao);
            pstmt.setInt(2, produtoId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                // Isso pode ser um problema se o produto/tamanho não for encontrado ou o estoque já estiver em 0 e tentando decrementar
                // Lançar uma exceção aqui pode ajudar a dar rollback na transação inteira se for uma condição de erro.
                System.err.println("Aviso: Nenhuma linha afetada ao atualizar estoque para produto ID " + produtoId + ", tamanho " + tamanho);
                // throw new SQLException("Falha ao atualizar estoque: produto ID " + produtoId + " não encontrado ou estoque inalterado.");
            }
        }
        // Não há commit ou rollback aqui; isso é gerenciado pelo chamador (salvarItensPedido)
    }


    // Renderer customizado para exibir os itens do carrinho de forma mais legível
    static class ItemCarrinhoRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ItemCarrinho) {
                ItemCarrinho item = (ItemCarrinho) value;
                setText(String.format("ID %d: %s (%s) - Qtd: %d - Preço: R$ %.2f - Subtotal: R$ %.2f",
                        item.getProdutoId(), item.getNomeProduto(), item.getTamanho(),
                        item.getQuantidade(), item.getValorUnitario(), item.getSubtotal()));
            }
            return this;
        }
    }

    public static void main(String[] args) {
        // Para teste, simular um usuário logado
        // SessaoUsuario.getInstance().iniciarSessao(1, "Cliente Teste", "cliente", "teste@exemplo.com");
        SwingUtilities.invokeLater(() -> new TelaCarrinho());
    }
}
