package com.exemplo; // Ou seu pacote correto, ex: Site

import com.exemplo.DBConnector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener; // Import explícito
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Para Statement.RETURN_GENERATED_KEYS (embora não usado aqui para update)
import java.util.ArrayList;
import java.util.HashMap; // Para carregar estoque
import java.util.List;
import java.util.Map;    // Para carregar estoque

public class TelaEditarProduto extends JFrame {

    private JTable produtosTable;
    private DefaultTableModel tableModel;
    private JButton selecionarImagem1Button, selecionarImagem2Button, selecionarImagem3Button;
    
    // Campos de edição
    private JTextField nomeProdutoField; // Adicionado para o nome do produto no painel de edição
    private JComboBox<String> tipoProdutoComboBoxEditar; // Para exibir/selecionar o tipo
    private JPanel tamanhosContainerPanelEditar;
    private CardLayout cardLayoutTamanhosEditar;

    // Campos para Roupas
    private JTextField quantidadePField;
    private JTextField quantidadeMField;
    private JTextField quantidadeGField;
    // Campos para Tênis
    private JTextField quantidadeNum38Field;
    private JTextField quantidadeNum39Field;
    private JTextField quantidadeNum40Field;
    private JTextField quantidadeNum41Field;
    private JTextField quantidadeNum42Field;

    private JTextField valorField;
    private JTextArea descricaoArea;
    private JButton salvarAlteracoesButton;
    private JButton removerProdutoButton;

    private List<String> newImagePaths = new ArrayList<>(3);
    private int selectedProductId = -1;
    private String currentTipoProduto = null; // Para saber qual painel de tamanho exibir
    private String currentImagePath1, currentImagePath2, currentImagePath3;

    private static final String ROUPA_PANEL_EDIT = "RoupaEditar";
    private static final String TENIS_PANEL_EDIT = "TênisEditar";
    private static final String IMAGES_COPY_DIRECTORY = "imagens_produtos_copiados";


    public TelaEditarProduto() {
        setTitle("Editar Produto");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(10,10));

        for (int i = 0; i < 3; i++) newImagePaths.add(null);

        // Tabela de Produtos - Removendo colunas de quantidade P, M, G daqui
        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Nome", "Tipo", "Valor", "Descrição", "Imagem 1", "Imagem 2", "Imagem 3"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Torna a tabela não editável
            }
        };
        produtosTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(produtosTable);
        add(tableScrollPane, BorderLayout.NORTH);

        // Painel de Edição
        JPanel edicaoPanel = new JPanel(new GridBagLayout());
        edicaoPanel.setBorder(BorderFactory.createTitledBorder("Detalhes do Produto Selecionado"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int yPosEdit = 0;

        // Nome do Produto (no painel de edição)
        gbc.gridx = 0; gbc.gridy = yPosEdit;
        edicaoPanel.add(new JLabel("Nome do Produto:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        nomeProdutoField = new JTextField(30); // Campo para nome
        edicaoPanel.add(nomeProdutoField, gbc);
        gbc.weightx = 0;
        yPosEdit++;
        
        // Tipo de Produto (no painel de edição - geralmente não editável, mas exibido)
        gbc.gridx = 0; gbc.gridy = yPosEdit;
        edicaoPanel.add(new JLabel("Tipo de Produto:"), gbc);
        gbc.gridx = 1;
        tipoProdutoComboBoxEditar = new JComboBox<>(new String[]{ROUPA_PANEL_EDIT, TENIS_PANEL_EDIT});
        // tipoProdutoComboBoxEditar.setEnabled(false); // Normalmente o tipo não muda após criação
        edicaoPanel.add(tipoProdutoComboBoxEditar, gbc);
        yPosEdit++;

        // Container para os campos de tamanho/numeração com CardLayout
        cardLayoutTamanhosEditar = new CardLayout();
        tamanhosContainerPanelEditar = new JPanel(cardLayoutTamanhosEditar);
        // ... (criação dos painéis painelRoupasEditar e painelTenisEditar, similar à TelaAdicionarProduto)
        // Painel para Roupas (P, M, G)
        JPanel painelRoupasEditar = new JPanel(new GridBagLayout()); /* ... adicione os campos ... */
        GridBagConstraints gbcRoupa = new GridBagConstraints();
        gbcRoupa.insets = new Insets(2,2,2,2); gbcRoupa.fill = GridBagConstraints.HORIZONTAL; gbcRoupa.anchor = GridBagConstraints.WEST;
        gbcRoupa.gridx = 0; gbcRoupa.gridy = 0; painelRoupasEditar.add(new JLabel("Qtd. (P):"), gbcRoupa);
        gbcRoupa.gridx = 1; quantidadePField = new JTextField(5); painelRoupasEditar.add(quantidadePField, gbcRoupa);
        gbcRoupa.gridx = 0; gbcRoupa.gridy = 1; painelRoupasEditar.add(new JLabel("Qtd. (M):"), gbcRoupa);
        gbcRoupa.gridx = 1; quantidadeMField = new JTextField(5); painelRoupasEditar.add(quantidadeMField, gbcRoupa);
        gbcRoupa.gridx = 0; gbcRoupa.gridy = 2; painelRoupasEditar.add(new JLabel("Qtd. (G):"), gbcRoupa);
        gbcRoupa.gridx = 1; quantidadeGField = new JTextField(5); painelRoupasEditar.add(quantidadeGField, gbcRoupa);
        tamanhosContainerPanelEditar.add(painelRoupasEditar, ROUPA_PANEL_EDIT);

        // Painel para Tênis (Numerações)
        JPanel painelTenisEditar = new JPanel(new GridBagLayout()); /* ... adicione os campos ... */
        GridBagConstraints gbcTenis = new GridBagConstraints();
        gbcTenis.insets = new Insets(2,2,2,2); gbcTenis.fill = GridBagConstraints.HORIZONTAL; gbcTenis.anchor = GridBagConstraints.WEST;
        gbcTenis.gridx = 0; gbcTenis.gridy = 0; painelTenisEditar.add(new JLabel("Qtd. (38):"), gbcTenis);
        gbcTenis.gridx = 1; quantidadeNum38Field = new JTextField(5); painelTenisEditar.add(quantidadeNum38Field, gbcTenis);
        gbcTenis.gridx = 0; gbcTenis.gridy = 1; painelTenisEditar.add(new JLabel("Qtd. (39):"), gbcTenis);
        gbcTenis.gridx = 1; quantidadeNum39Field = new JTextField(5); painelTenisEditar.add(quantidadeNum39Field, gbcTenis);
        gbcTenis.gridx = 0; gbcTenis.gridy = 2; painelTenisEditar.add(new JLabel("Qtd. (40):"), gbcTenis);
        gbcTenis.gridx = 1; quantidadeNum40Field = new JTextField(5); painelTenisEditar.add(quantidadeNum40Field, gbcTenis);
        gbcTenis.gridx = 0; gbcTenis.gridy = 3; painelTenisEditar.add(new JLabel("Qtd. (41):"), gbcTenis);
        gbcTenis.gridx = 1; quantidadeNum41Field = new JTextField(5); painelTenisEditar.add(quantidadeNum41Field, gbcTenis);
        gbcTenis.gridx = 0; gbcTenis.gridy = 4; painelTenisEditar.add(new JLabel("Qtd. (42):"), gbcTenis);
        gbcTenis.gridx = 1; quantidadeNum42Field = new JTextField(5); painelTenisEditar.add(quantidadeNum42Field, gbcTenis);
        tamanhosContainerPanelEditar.add(painelTenisEditar, TENIS_PANEL_EDIT);

        gbc.gridx = 0; gbc.gridy = yPosEdit;
        gbc.gridwidth = 2;
        edicaoPanel.add(tamanhosContainerPanelEditar, gbc);
        gbc.gridwidth = 1;
        yPosEdit++;

        // Listener para o tipoProdutoComboBoxEditar (se for editável)
        tipoProdutoComboBoxEditar.addActionListener(e -> {
            String selecionado = (String) tipoProdutoComboBoxEditar.getSelectedItem();
            cardLayoutTamanhosEditar.show(tamanhosContainerPanelEditar, selecionado);
        });


        // Valor
        gbc.gridx = 0; gbc.gridy = yPosEdit;
        edicaoPanel.add(new JLabel("Valor:"), gbc);
        gbc.gridx = 1;
        valorField = new JTextField(10);
        edicaoPanel.add(valorField, gbc);
        yPosEdit++;

        // Descrição
        gbc.gridx = 0; gbc.gridy = yPosEdit; gbc.anchor = GridBagConstraints.NORTHWEST;
        edicaoPanel.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        descricaoArea = new JTextArea(5, 20);
        // ... (configurações descricaoArea)
        JScrollPane descricaoScrollPane = new JScrollPane(descricaoArea);
        edicaoPanel.add(descricaoScrollPane, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0; // Reset
        yPosEdit++;
        
        // Botões de seleção de imagem
        gbc.gridx = 0; gbc.gridy = yPosEdit;
        edicaoPanel.add(new JLabel("Imagem 1:"), gbc);
        gbc.gridx = 1; selecionarImagem1Button = new JButton("Alterar Imagem 1"); edicaoPanel.add(selecionarImagem1Button, gbc); yPosEdit++;
        gbc.gridx = 0; gbc.gridy = yPosEdit;
        edicaoPanel.add(new JLabel("Imagem 2:"), gbc);
        gbc.gridx = 1; selecionarImagem2Button = new JButton("Alterar Imagem 2"); edicaoPanel.add(selecionarImagem2Button, gbc); yPosEdit++;
        gbc.gridx = 0; gbc.gridy = yPosEdit;
        edicaoPanel.add(new JLabel("Imagem 3:"), gbc);
        gbc.gridx = 1; selecionarImagem3Button = new JButton("Alterar Imagem 3"); edicaoPanel.add(selecionarImagem3Button, gbc); yPosEdit++;


        // Botões Salvar e Remover
        gbc.gridx = 0; gbc.gridy = yPosEdit; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JPanel botoesAcaoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        salvarAlteracoesButton = new JButton("Salvar Alterações");
        removerProdutoButton = new JButton("Remover Produto");
        botoesAcaoPanel.add(salvarAlteracoesButton);
        botoesAcaoPanel.add(removerProdutoButton);
        edicaoPanel.add(botoesAcaoPanel, gbc);

        add(new JScrollPane(edicaoPanel), BorderLayout.CENTER);

        carregarProdutos(); // Carrega produtos na tabela

        selecionarImagem1Button.addActionListener(e -> selecionarNovaImagem(0)); // Usa índice 0
        selecionarImagem2Button.addActionListener(e -> selecionarNovaImagem(1)); // Usa índice 1
        selecionarImagem3Button.addActionListener(e -> selecionarNovaImagem(2)); // Usa índice 2

        produtosTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && produtosTable.getSelectedRow() != -1) {
                int selectedRow = produtosTable.getSelectedRow();
                selectedProductId = (int) tableModel.getValueAt(selectedRow, 0); // Coluna ID
                // Carregar detalhes do produto e suas variações de estoque
                carregarDetalhesProdutoSelecionado(selectedProductId);
            }
        });

        salvarAlteracoesButton.addActionListener(e -> {
            if (selectedProductId != -1) {
                salvarAlteracoesProduto();
            } else {
                JOptionPane.showMessageDialog(TelaEditarProduto.this, "Selecione um produto para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        removerProdutoButton.addActionListener(e -> {
            if (selectedProductId != -1) {
                removerProdutoDoBanco();
            } else {
                JOptionPane.showMessageDialog(TelaEditarProduto.this, "Selecione um produto para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        setVisible(true);
    }

    // 👇 MÉTODO CARREGAR PRODUTOS CORRIGIDO PARA OPÇÃO B
    private void carregarProdutos() {
        tableModel.setRowCount(0); // Limpa a tabela
        DBConnector dbConnector = new DBConnector();
        // Seleciona apenas dados da tabela 'produtos', incluindo 'tipo_produto'
        // NÃO seleciona mais quantidade_p, _m, _g diretamente daqui.
        String sql = "SELECT id, nome, tipo_produto, valor, descricao, imagens1_path, imagens2_path, imagens3_path FROM produtos ORDER BY id";

        try (Connection conn = dbConnector.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("tipo_produto"), // Exibe o tipo
                        rs.getDouble("valor"),
                        rs.getString("descricao"),
                        rs.getString("imagens1_path"),
                        rs.getString("imagens2_path"),
                        rs.getString("imagens3_path")
                        // Quantidades P, M, G não são mais colunas diretas da tabela aqui
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar produtos: " + e.getMessage(), "Erro SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // 👇 NOVO MÉTODO PARA CARREGAR DETALHES E ESTOQUE DO PRODUTO SELECIONADO
    private void carregarDetalhesProdutoSelecionado(int produtoId) {
        DBConnector dbConnector = new DBConnector();
        // 1. Carregar dados principais do produto
        String sqlProduto = "SELECT nome, tipo_produto, valor, descricao, imagens1_path, imagens2_path, imagens3_path FROM produtos WHERE id = ?";
        
        try (Connection conn = dbConnector.conectar();
             PreparedStatement pstmtProduto = conn.prepareStatement(sqlProduto)) {
            
            pstmtProduto.setInt(1, produtoId);
            try (ResultSet rsProduto = pstmtProduto.executeQuery()) {
                if (rsProduto.next()) {
                    nomeProdutoField.setText(rsProduto.getString("nome"));
                    currentTipoProduto = rsProduto.getString("tipo_produto");
                    valorField.setText(String.valueOf(rsProduto.getDouble("valor")));
                    descricaoArea.setText(rsProduto.getString("descricao"));
                    currentImagePath1 = rsProduto.getString("imagens1_path");
                    currentImagePath2 = rsProduto.getString("imagens2_path");
                    currentImagePath3 = rsProduto.getString("imagens3_path");
                    newImagePaths.clear(); // Limpa para novas seleções de imagem
                    for(int i=0; i<3; i++) newImagePaths.add(null); // Reset
                    
                    // Atualiza o ComboBox e o CardLayout para o tipo do produto
                    if (ROUPA_PANEL_EDIT.equals(currentTipoProduto) || "ROUPA".equalsIgnoreCase(currentTipoProduto)) {
                        tipoProdutoComboBoxEditar.setSelectedItem(ROUPA_PANEL_EDIT);
                        cardLayoutTamanhosEditar.show(tamanhosContainerPanelEditar, ROUPA_PANEL_EDIT);
                    } else if (TENIS_PANEL_EDIT.equals(currentTipoProduto) || "TENIS".equalsIgnoreCase(currentTipoProduto)) {
                        tipoProdutoComboBoxEditar.setSelectedItem(TENIS_PANEL_EDIT);
                        cardLayoutTamanhosEditar.show(tamanhosContainerPanelEditar, TENIS_PANEL_EDIT);
                    }
                } else {
                    // Produto não encontrado, limpar campos de edição
                    limparCamposEdicao();
                    return;
                }
            }

            // 2. Carregar quantidades da tabela 'estoque_variacoes'
            String sqlEstoque = "SELECT tamanho_descricao, quantidade FROM estoque_variacoes WHERE produto_id = ?";
            Map<String, Integer> estoqueMap = new HashMap<>();
            try (PreparedStatement pstmtEstoque = conn.prepareStatement(sqlEstoque)) {
                pstmtEstoque.setInt(1, produtoId);
                try (ResultSet rsEstoque = pstmtEstoque.executeQuery()) {
                    while (rsEstoque.next()) {
                        estoqueMap.put(rsEstoque.getString("tamanho_descricao").toUpperCase(), rsEstoque.getInt("quantidade"));
                    }
                }
            }

            // Preencher os campos de quantidade com base no tipo e no que foi carregado
            limparCamposDeQuantidade(); // Limpa antes de preencher
            if (ROUPA_PANEL_EDIT.equals(tipoProdutoComboBoxEditar.getSelectedItem())) {
                quantidadePField.setText(String.valueOf(estoqueMap.getOrDefault("P", 0)));
                quantidadeMField.setText(String.valueOf(estoqueMap.getOrDefault("M", 0)));
                quantidadeGField.setText(String.valueOf(estoqueMap.getOrDefault("G", 0)));
            } else if (TENIS_PANEL_EDIT.equals(tipoProdutoComboBoxEditar.getSelectedItem())) {
                quantidadeNum38Field.setText(String.valueOf(estoqueMap.getOrDefault("38", 0)));
                quantidadeNum39Field.setText(String.valueOf(estoqueMap.getOrDefault("39", 0)));
                quantidadeNum40Field.setText(String.valueOf(estoqueMap.getOrDefault("40", 0)));
                quantidadeNum41Field.setText(String.valueOf(estoqueMap.getOrDefault("41", 0)));
                quantidadeNum42Field.setText(String.valueOf(estoqueMap.getOrDefault("42", 0)));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar detalhes do produto: " + e.getMessage(), "Erro SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            limparCamposEdicao();
        }
    }
    
    private void limparCamposDeQuantidade() {
        quantidadePField.setText("0");
        quantidadeMField.setText("0");
        quantidadeGField.setText("0");
        quantidadeNum38Field.setText("0");
        quantidadeNum39Field.setText("0");
        quantidadeNum40Field.setText("0");
        quantidadeNum41Field.setText("0");
        quantidadeNum42Field.setText("0");
    }


    private void selecionarNovaImagem(int imagemIndex) { // imagemIndex é 0, 1, ou 2
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Nova Imagem " + (imagemIndex + 1));
        // ... (resto do método como antes) ...
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            newImagePaths.set(imagemIndex, selectedFile.getAbsolutePath());
            // Opcional: atualizar um label para mostrar o nome do arquivo selecionado
            JOptionPane.showMessageDialog(this, "Nova imagem " + (imagemIndex + 1) + " selecionada: " + selectedFile.getName(), "Imagem Selecionada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // 👇 MÉTODO SALVAR ALTERAÇÕES CORRIGIDO PARA OPÇÃO B
    private void salvarAlteracoesProduto() {
        if (selectedProductId == -1) {
            JOptionPane.showMessageDialog(this, "Nenhum produto selecionado para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nome = nomeProdutoField.getText().trim(); // Usar o campo de nome do painel de edição
        String valorStr = valorField.getText().trim();
        String descricao = descricaoArea.getText().trim();
        String tipoProdutoSelecionado = (String) tipoProdutoComboBoxEditar.getSelectedItem(); // Tipo do painel de edição

        // Validações básicas
        if (nome.isEmpty() || valorStr.isEmpty() || descricao.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome, Valor e Descrição são obrigatórios.", "Campos Obrigatórios", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Connection conn = null;
        DBConnector dbConnector = new DBConnector();

        try {
            double valor = Double.parseDouble(valorStr.replace(",", "."));
            if (valor < 0) throw new NumberFormatException("Valor não pode ser negativo.");

            // Copiar imagens novas, se houver, e obter seus caminhos
            // Se newImagePaths.get(i) for null, significa que o usuário não selecionou uma nova imagem para aquele slot
            String finalImagePath1 = (newImagePaths.size() > 0 && newImagePaths.get(0) != null) ? salvarImagemSeSelecionada(0, currentImagePath1) : currentImagePath1;
            String finalImagePath2 = (newImagePaths.size() > 1 && newImagePaths.get(1) != null) ? salvarImagemSeSelecionada(1, currentImagePath2) : currentImagePath2;
            String finalImagePath3 = (newImagePaths.size() > 2 && newImagePaths.get(2) != null) ? salvarImagemSeSelecionada(2, currentImagePath3) : currentImagePath3;

            conn = dbConnector.conectar();
            conn.setAutoCommit(false); // Inicia transação

            // 1. ATUALIZAR TABELA 'produtos'
            // (Assume que tipo_produto pode ser alterado, se não, remova-o do UPDATE)
            String sqlProdutoUpdate = """
                    UPDATE produtos SET nome = ?, descricao = ?, valor = ?, tipo_produto = ?, 
                    imagens1_path = ?, imagens2_path = ?, imagens3_path = ? 
                    WHERE id = ?
                    """;
            try (PreparedStatement pstmtUpdateProduto = conn.prepareStatement(sqlProdutoUpdate)) {
                pstmtUpdateProduto.setString(1, nome);
                pstmtUpdateProduto.setString(2, descricao);
                pstmtUpdateProduto.setDouble(3, valor);
                pstmtUpdateProduto.setString(4, tipoProdutoSelecionado.equals(ROUPA_PANEL_EDIT) ? "ROUPA" : "TENIS");
                pstmtUpdateProduto.setString(5, finalImagePath1);
                pstmtUpdateProduto.setString(6, finalImagePath2);
                pstmtUpdateProduto.setString(7, finalImagePath3);
                pstmtUpdateProduto.setInt(8, selectedProductId);
                pstmtUpdateProduto.executeUpdate();
            }

            // 2. ATUALIZAR TABELA 'estoque_variacoes'
            // Estratégia: Deletar todas as variações antigas e inserir as novas.
            // Isso simplifica a lógica de ter que verificar quais atualizar, quais inserir, quais deletar.
            String sqlDeleteEstoque = "DELETE FROM estoque_variacoes WHERE produto_id = ?";
            try (PreparedStatement pstmtDeleteEstoque = conn.prepareStatement(sqlDeleteEstoque)) {
                pstmtDeleteEstoque.setInt(1, selectedProductId);
                pstmtDeleteEstoque.executeUpdate();
            }

            String sqlInsertEstoque = "INSERT INTO estoque_variacoes (produto_id, tamanho_descricao, quantidade) VALUES (?, ?, ?)";
            try (PreparedStatement pstmtInsertEstoque = conn.prepareStatement(sqlInsertEstoque)) {
                 if (ROUPA_PANEL_EDIT.equals(tipoProdutoSelecionado)) {
                    inserirEstoqueSeValido(pstmtInsertEstoque, selectedProductId, "P", quantidadePField.getText());
                    inserirEstoqueSeValido(pstmtInsertEstoque, selectedProductId, "M", quantidadeMField.getText());
                    inserirEstoqueSeValido(pstmtInsertEstoque, selectedProductId, "G", quantidadeGField.getText());
                } else if (TENIS_PANEL_EDIT.equals(tipoProdutoSelecionado)) {
                    inserirEstoqueSeValido(pstmtInsertEstoque, selectedProductId, "38", quantidadeNum38Field.getText());
                    inserirEstoqueSeValido(pstmtInsertEstoque, selectedProductId, "39", quantidadeNum39Field.getText());
                    inserirEstoqueSeValido(pstmtInsertEstoque, selectedProductId, "40", quantidadeNum40Field.getText());
                    inserirEstoqueSeValido(pstmtInsertEstoque, selectedProductId, "41", quantidadeNum41Field.getText());
                    inserirEstoqueSeValido(pstmtInsertEstoque, selectedProductId, "42", quantidadeNum42Field.getText());
                }
                pstmtInsertEstoque.executeBatch();
            }

            conn.commit(); // Confirma a transação
            JOptionPane.showMessageDialog(this, "Produto atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarProdutos(); // Recarrega a tabela de produtos
            limparCamposEdicao(); // Limpa os campos do formulário de edição
            newImagePaths.clear(); for(int i=0; i<3; i++) newImagePaths.add(null); // Reseta


        } catch (NumberFormatException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException se) { System.err.println("Erro no rollback: " + se.getMessage()); }
            JOptionPane.showMessageDialog(this, "Quantidades e valor devem ser números válidos. " + ex.getMessage(), "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException se) { System.err.println("Erro no rollback: " + se.getMessage()); }
            JOptionPane.showMessageDialog(this, "Erro no banco de dados ao salvar: " + ex.getMessage(), "Erro SQL", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (IOException ex) { // Para salvarImagemSeSelecionada
             if (conn != null) try { conn.rollback(); } catch (SQLException se) { System.err.println("Erro no rollback: " + se.getMessage()); }
            JOptionPane.showMessageDialog(this, "Erro ao manipular imagens: " + ex.getMessage(), "Erro de Arquivo", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
         finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // Método auxiliar de TelaAdicionarProduto, adaptado aqui se necessário
    private void inserirEstoqueSeValido(PreparedStatement pstmt, int produtoId, String tamanhoDesc, String quantidadeStr) throws SQLException, NumberFormatException {
        String qtdTrimmed = quantidadeStr.trim();
        if (!qtdTrimmed.isEmpty()) {
            int quantidade = Integer.parseInt(qtdTrimmed);
            if (quantidade < 0) {
                throw new NumberFormatException("Quantidade não pode ser negativa para o tamanho " + tamanhoDesc);
            }
            pstmt.setInt(1, produtoId);
            pstmt.setString(2, tamanhoDesc);
            pstmt.setInt(3, quantidade);
            pstmt.addBatch();
        }
        // Se estiver vazio, não insere estoque para esse tamanho (poderia ser interpretado como 0 ou não existente)
    }


    private String salvarImagemSeSelecionada(int index, String currentPath) throws IOException {
        // ... (mesma lógica de TelaAdicionarProduto, ajustada para IOException)
        if (newImagePaths.size() > index && newImagePaths.get(index) != null) {
            File origem = new File(newImagePaths.get(index));
            String nomeOriginal = origem.getName();
            String extensao = "";
            int i = nomeOriginal.lastIndexOf('.');
            if (i > 0 && i < nomeOriginal.length() - 1) {
                extensao = nomeOriginal.substring(i);
            }
            String nomeArquivoUnico = System.currentTimeMillis() + "_edit_img" + (index + 1) + extensao;
            Path diretorioDestinoPath = Paths.get(IMAGES_COPY_DIRECTORY);
            if (!Files.exists(diretorioDestinoPath)) {
                Files.createDirectories(diretorioDestinoPath);
            }
            Path destination = diretorioDestinoPath.resolve(nomeArquivoUnico);
            Files.copy(origem.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            return IMAGES_COPY_DIRECTORY + File.separator + nomeArquivoUnico;
        }
        return currentPath;
    }

    private void removerProdutoDoBanco() {
        // ... (lógica como antes, usando DBConnector e try-with-resources)
        // Lembre-se que ON DELETE CASCADE na tabela estoque_variacoes removerá o estoque automaticamente.
         if (selectedProductId == -1) {
            JOptionPane.showMessageDialog(this, "Nenhum produto selecionado para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja remover este produto?\nID: " + selectedProductId + "\nIsso também removerá todo o seu estoque associado.", "Confirmação de Remoção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            DBConnector dbConnector = new DBConnector();
            String sql = "DELETE FROM produtos WHERE id = ?";
            try (Connection conn = dbConnector.conectar();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedProductId);
                int rowsDeleted = pstmt.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Produto removido com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    carregarProdutos();
                    limparCamposEdicao();
                    selectedProductId = -1;
                    newImagePaths.clear(); for(int i=0; i<3; i++) newImagePaths.add(null); // Reseta
                } else {
                    JOptionPane.showMessageDialog(this, "Falha ao remover o produto.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro no banco de dados ao remover: " + ex.getMessage(), "Erro SQL", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void limparCamposEdicao() {
        nomeProdutoField.setText("");
        tipoProdutoComboBoxEditar.setSelectedIndex(0);
        valorField.setText("");
        descricaoArea.setText("");
        limparCamposDeQuantidade();
        // Limpar previews de imagem se você os tiver
        currentImagePath1 = null;
        currentImagePath2 = null;
        currentImagePath3 = null;
        produtosTable.clearSelection();
        selectedProductId = -1;
        cardLayoutTamanhosEditar.show(tamanhosContainerPanelEditar, ROUPA_PANEL_EDIT);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TelaEditarProduto::new);
    }
}