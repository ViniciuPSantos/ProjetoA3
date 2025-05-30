package com.exemplo;

import com.exemplo.DBConnector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
// Removido ActionListener explícito pois usamos referência de método this::salvarProdutoNoBanco
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // Necessário para getGeneratedKeys
import java.sql.SQLException;
import java.sql.Statement;  // Necessário para RETURN_GENERATED_KEYS
import java.util.ArrayList;
import java.util.List;

public class TelaAdicionarProduto extends JFrame {

    private JTextField nomeField;
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
    private JButton salvarProdutoButton;
    private List<String> imagePaths;
    private JButton adicionarImagem1Button, adicionarImagem2Button, adicionarImagem3Button;
    private JLabel[] statusLabelsImagens;

    private JComboBox<String> tipoProdutoComboBox;
    private JPanel tamanhosContainerPanel;
    private CardLayout cardLayoutTamanhos;

    private static final String ROUPA_PANEL = "Roupa";
    private static final String TENIS_PANEL = "Tênis";
    private static final String IMAGES_COPY_DIRECTORY = "imagens_produtos_copiados";

    public TelaAdicionarProduto() {
        setTitle("Adicionar Novo Produto");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        imagePaths = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) imagePaths.add(null);
        statusLabelsImagens = new JLabel[3];

        JPanel imageSelectionPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        imageSelectionPanel.setBorder(BorderFactory.createTitledBorder("Imagens do Produto"));
        adicionarImagem1Button = new JButton("Selecionar Imagem 1");
        adicionarImagem2Button = new JButton("Selecionar Imagem 2");
        adicionarImagem3Button = new JButton("Selecionar Imagem 3");
        imageSelectionPanel.add(criarPainelSelecaoImagem(adicionarImagem1Button, 0));
        imageSelectionPanel.add(criarPainelSelecaoImagem(adicionarImagem2Button, 1));
        imageSelectionPanel.add(criarPainelSelecaoImagem(adicionarImagem3Button, 2));
        add(imageSelectionPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int yPos = 0;

        gbc.gridx = 0; gbc.gridy = yPos;
        inputPanel.add(new JLabel("Nome do Produto:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        nomeField = new JTextField(20);
        inputPanel.add(nomeField, gbc);
        gbc.weightx = 0;
        yPos++;

        gbc.gridx = 0; gbc.gridy = yPos;
        inputPanel.add(new JLabel("Tipo de Produto:"), gbc);
        gbc.gridx = 1;
        tipoProdutoComboBox = new JComboBox<>(new String[]{ROUPA_PANEL, TENIS_PANEL});
        inputPanel.add(tipoProdutoComboBox, gbc);
        yPos++;

        cardLayoutTamanhos = new CardLayout();
        tamanhosContainerPanel = new JPanel(cardLayoutTamanhos);

        JPanel painelRoupas = new JPanel(new GridBagLayout());
        GridBagConstraints gbcRoupa = new GridBagConstraints();
        gbcRoupa.insets = new Insets(2,2,2,2); gbcRoupa.fill = GridBagConstraints.HORIZONTAL; gbcRoupa.anchor = GridBagConstraints.WEST;
        gbcRoupa.gridx = 0; gbcRoupa.gridy = 0; painelRoupas.add(new JLabel("Qtd. (P):"), gbcRoupa);
        gbcRoupa.gridx = 1; quantidadePField = new JTextField(5); painelRoupas.add(quantidadePField, gbcRoupa);
        gbcRoupa.gridx = 0; gbcRoupa.gridy = 1; painelRoupas.add(new JLabel("Qtd. (M):"), gbcRoupa);
        gbcRoupa.gridx = 1; quantidadeMField = new JTextField(5); painelRoupas.add(quantidadeMField, gbcRoupa);
        gbcRoupa.gridx = 0; gbcRoupa.gridy = 2; painelRoupas.add(new JLabel("Qtd. (G):"), gbcRoupa);
        gbcRoupa.gridx = 1; quantidadeGField = new JTextField(5); painelRoupas.add(quantidadeGField, gbcRoupa);
        tamanhosContainerPanel.add(painelRoupas, ROUPA_PANEL);

        JPanel painelTenis = new JPanel(new GridBagLayout());
        GridBagConstraints gbcTenis = new GridBagConstraints();
        gbcTenis.insets = new Insets(2,2,2,2); gbcTenis.fill = GridBagConstraints.HORIZONTAL; gbcTenis.anchor = GridBagConstraints.WEST;
        gbcTenis.gridx = 0; gbcTenis.gridy = 0; painelTenis.add(new JLabel("Qtd. (38):"), gbcTenis);
        gbcTenis.gridx = 1; quantidadeNum38Field = new JTextField(5); painelTenis.add(quantidadeNum38Field, gbcTenis);
        gbcTenis.gridx = 0; gbcTenis.gridy = 1; painelTenis.add(new JLabel("Qtd. (39):"), gbcTenis);
        gbcTenis.gridx = 1; quantidadeNum39Field = new JTextField(5); painelTenis.add(quantidadeNum39Field, gbcTenis);
        gbcTenis.gridx = 0; gbcTenis.gridy = 2; painelTenis.add(new JLabel("Qtd. (40):"), gbcTenis);
        gbcTenis.gridx = 1; quantidadeNum40Field = new JTextField(5); painelTenis.add(quantidadeNum40Field, gbcTenis);
        gbcTenis.gridx = 0; gbcTenis.gridy = 3; painelTenis.add(new JLabel("Qtd. (41):"), gbcTenis);
        gbcTenis.gridx = 1; quantidadeNum41Field = new JTextField(5); painelTenis.add(quantidadeNum41Field, gbcTenis);
        gbcTenis.gridx = 0; gbcTenis.gridy = 4; painelTenis.add(new JLabel("Qtd. (42):"), gbcTenis);
        gbcTenis.gridx = 1; quantidadeNum42Field = new JTextField(5); painelTenis.add(quantidadeNum42Field, gbcTenis);
        tamanhosContainerPanel.add(painelTenis, TENIS_PANEL);

        gbc.gridx = 0; gbc.gridy = yPos;
        gbc.gridwidth = 2;
        inputPanel.add(tamanhosContainerPanel, gbc);
        gbc.gridwidth = 1;
        yPos++;

        gbc.gridx = 0; gbc.gridy = yPos;
        inputPanel.add(new JLabel("Valor (R$):"), gbc);
        gbc.gridx = 1;
        valorField = new JTextField(10);
        inputPanel.add(valorField, gbc);
        yPos++;

        gbc.gridx = 0; gbc.gridy = yPos;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        inputPanel.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1; 
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        descricaoArea = new JTextArea(5, 20);
        descricaoArea.setLineWrap(true);
        descricaoArea.setWrapStyleWord(true);
        JScrollPane descricaoScrollPane = new JScrollPane(descricaoArea);
        inputPanel.add(descricaoScrollPane, gbc);
        
        add(new JScrollPane(inputPanel), BorderLayout.CENTER);

        salvarProdutoButton = new JButton("Salvar Produto");
        salvarProdutoButton.setFont(new Font("Arial", Font.BOLD, 14));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBorder(new EmptyBorder(10,0,10,0));
        bottomPanel.add(salvarProdutoButton);
        add(bottomPanel, BorderLayout.SOUTH);

        tipoProdutoComboBox.addActionListener(e -> {
            String selecionado = (String) tipoProdutoComboBox.getSelectedItem();
            cardLayoutTamanhos.show(tamanhosContainerPanel, selecionado);
        });
        cardLayoutTamanhos.show(tamanhosContainerPanel, ROUPA_PANEL);

        adicionarImagem1Button.addActionListener(e -> selecionarImagem(0));
        adicionarImagem2Button.addActionListener(e -> selecionarImagem(1));
        adicionarImagem3Button.addActionListener(e -> selecionarImagem(2));
        salvarProdutoButton.addActionListener(this::salvarProdutoNoBanco);

        setVisible(true);
    }

    private JPanel criarPainelSelecaoImagem(JButton botao, int index) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        botao.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabelsImagens[index] = new JLabel("(Nenhuma imagem selecionada)");
        statusLabelsImagens[index].setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabelsImagens[index].setFont(new Font("Arial", Font.ITALIC, 10));
        panel.add(botao);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(statusLabelsImagens[index]);
        return panel;
    }

    private void selecionarImagem(int imagemIndex) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Imagem " + (imagemIndex + 1));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagens", "jpg", "jpeg", "png", "gif"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            imagePaths.set(imagemIndex, selectedFile.getAbsolutePath());
            statusLabelsImagens[imagemIndex].setText(selectedFile.getName());
            statusLabelsImagens[imagemIndex].setToolTipText(selectedFile.getAbsolutePath());
        }
    }

    // 👇 MÉTODO SALVAR PRODUTO CORRIGIDO PARA OPÇÃO B (com tabela estoque_variacoes) 👇
    private void salvarProdutoNoBanco(ActionEvent e) {
        String nome = nomeField.getText().trim();
        String valorStr = valorField.getText().trim();
        String descricao = descricaoArea.getText().trim();
        String tipoProdutoSelecionado = (String) tipoProdutoComboBox.getSelectedItem();

        boolean algumaImagemSelecionada = imagePaths.stream().anyMatch(p -> p != null && !p.isEmpty());
        if (nome.isEmpty() || valorStr.isEmpty() || descricao.isEmpty() || !algumaImagemSelecionada) {
            JOptionPane.showMessageDialog(this, "Preencha Nome, Valor, Descrição e selecione ao menos a Imagem 1.", "Campos Obrigatórios", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = null;
        DBConnector dbConnector = new DBConnector();

        try {
            double valor = Double.parseDouble(valorStr.replace(",", "."));
            if (valor < 0) {
                JOptionPane.showMessageDialog(this, "O valor não pode ser negativo.", "Valor Inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<String> caminhosRelativosSalvos = copiarImagensParaDiretorio();

            conn = dbConnector.conectar();
            conn.setAutoCommit(false); // Inicia a transação

            // 1. INSERIR NA TABELA 'produtos' (sem as colunas de quantidade direta)
            //    Certifique-se que sua tabela 'produtos' tem a coluna 'tipo_produto'
            String sqlProduto = """
                    INSERT INTO produtos 
                    (nome, descricao, valor, tipo_produto, 
                     imagens1_path, imagens2_path, imagens3_path) 
                    VALUES (?, ?, ?, ?, ?, ?, ?) 
                    """;
            int produtoIdGerado = -1;

            try (PreparedStatement pstmtProduto = conn.prepareStatement(sqlProduto, Statement.RETURN_GENERATED_KEYS)) {
                pstmtProduto.setString(1, nome);
                pstmtProduto.setString(2, descricao);
                pstmtProduto.setDouble(3, valor);
                pstmtProduto.setString(4, tipoProdutoSelecionado); // Salva o tipo 'ROUPA' ou 'TÊNIS'
                
                pstmtProduto.setString(5, caminhosRelativosSalvos.size() > 0 && caminhosRelativosSalvos.get(0) != null ? caminhosRelativosSalvos.get(0) : null);
                pstmtProduto.setString(6, caminhosRelativosSalvos.size() > 1 && caminhosRelativosSalvos.get(1) != null ? caminhosRelativosSalvos.get(1) : null);
                pstmtProduto.setString(7, caminhosRelativosSalvos.size() > 2 && caminhosRelativosSalvos.get(2) != null ? caminhosRelativosSalvos.get(2) : null);

                int rowsAffected = pstmtProduto.executeUpdate();
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = pstmtProduto.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            produtoIdGerado = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Falha ao obter o ID do produto inserido, nenhum ID obtido.");
                        }
                    }
                } else {
                    throw new SQLException("Falha ao inserir o produto na tabela 'produtos', nenhuma linha afetada.");
                }
            }

            // 2. INSERIR NA TABELA 'estoque_variacoes'
            // Certifique-se que sua tabela 'estoque_variacoes' existe com (produto_id, tamanho_descricao, quantidade)
            String sqlEstoque = "INSERT INTO estoque_variacoes (produto_id, tamanho_descricao, quantidade) VALUES (?, ?, ?)";
            try (PreparedStatement pstmtEstoque = conn.prepareStatement(sqlEstoque)) {
                if (ROUPA_PANEL.equals(tipoProdutoSelecionado)) {
                    inserirEstoqueSeValido(pstmtEstoque, produtoIdGerado, "P", quantidadePField.getText());
                    inserirEstoqueSeValido(pstmtEstoque, produtoIdGerado, "M", quantidadeMField.getText());
                    inserirEstoqueSeValido(pstmtEstoque, produtoIdGerado, "G", quantidadeGField.getText());
                } else if (TENIS_PANEL.equals(tipoProdutoSelecionado)) {
                    inserirEstoqueSeValido(pstmtEstoque, produtoIdGerado, "38", quantidadeNum38Field.getText());
                    inserirEstoqueSeValido(pstmtEstoque, produtoIdGerado, "39", quantidadeNum39Field.getText());
                    inserirEstoqueSeValido(pstmtEstoque, produtoIdGerado, "40", quantidadeNum40Field.getText());
                    inserirEstoqueSeValido(pstmtEstoque, produtoIdGerado, "41", quantidadeNum41Field.getText());
                    inserirEstoqueSeValido(pstmtEstoque, produtoIdGerado, "42", quantidadeNum42Field.getText());
                }
                pstmtEstoque.executeBatch(); // Executa todos os inserts de estoque de uma vez
            }

            conn.commit(); // Confirma a transação se tudo deu certo
            JOptionPane.showMessageDialog(this, "Produto '" + nome + "' e seu estoque salvos com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();

        } catch (NumberFormatException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException se) { System.err.println("Erro no rollback após NumberFormatException: " + se.getMessage()); }
            JOptionPane.showMessageDialog(this, "Quantidades e valor devem ser números válidos.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException se) { System.err.println("Erro no rollback após SQLException: " + se.getMessage()); }
            JOptionPane.showMessageDialog(this, "Erro no banco de dados: " + ex.getMessage(), "Erro SQL", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // MUITO IMPORTANTE para ver detalhes do erro SQL no console
        } catch (IOException ex) {
            // Se a cópia de imagem falhar, a transação do banco não deve ter sido iniciada ou deve ser desfeita.
            if (conn != null) try { conn.rollback(); } catch (SQLException se) { System.err.println("Erro no rollback após IOException: " + se.getMessage()); }
            JOptionPane.showMessageDialog(this, "Erro ao copiar imagens: " + ex.getMessage(), "Erro de Arquivo", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaura o modo de autoCommit
                    conn.close(); // Fecha a conexão
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // Método auxiliar para inserir na tabela estoque_variacoes
    private void inserirEstoqueSeValido(PreparedStatement pstmt, int produtoId, String tamanhoDesc, String quantidadeStr) throws SQLException, NumberFormatException {
        String qtdTrimmed = quantidadeStr.trim();
        if (!qtdTrimmed.isEmpty()) {
            // A conversão para int e a verificação se > 0 já é feita na chamada do método salvarProdutoNoBanco
            // Aqui, só precisamos garantir que a string não está vazia antes de tentar converter
            // No entanto, para maior robustez e para o batch, é melhor fazer a conversão aqui.
            int quantidade = Integer.parseInt(qtdTrimmed); // Pode lançar NumberFormatException
            if (quantidade < 0) {
                 // Você pode decidir lançar uma exceção ou apenas ignorar/logar quantidades negativas
                System.err.println("Aviso: Quantidade negativa '" + quantidade + "' para o tamanho " + tamanhoDesc + " do produto ID " + produtoId + " não será inserida.");
                return; // Ou throw new IllegalArgumentException("Quantidade não pode ser negativa.");
            }
            // Se a quantidade for 0, você pode decidir inserir ou não.
            // O código atual insere se for 0. Se não quiser inserir 0, adicione: if (quantidade == 0) return;

            pstmt.setInt(1, produtoId);
            pstmt.setString(2, tamanhoDesc);
            pstmt.setInt(3, quantidade);
            pstmt.addBatch();
        }
        // Se a string de quantidade estiver vazia, não faz nada para esse tamanho.
    }


    private List<String> copiarImagensParaDiretorio() throws IOException {
        List<String> caminhosRelativosSalvosNoBanco = new ArrayList<>(3);
        for(int i=0; i<3; i++) caminhosRelativosSalvosNoBanco.add(null);

        Path diretorioDestino = Paths.get(IMAGES_COPY_DIRECTORY);

        if (!Files.exists(diretorioDestino)) {
            Files.createDirectories(diretorioDestino);
        }

        for (int i = 0; i < imagePaths.size(); i++) {
            String sourcePathStr = imagePaths.get(i);
            if (sourcePathStr != null && !sourcePathStr.isEmpty()) {
                Path source = Paths.get(sourcePathStr);
                String nomeOriginal = source.getFileName().toString();
                String extensao = "";
                int dotIndex = nomeOriginal.lastIndexOf('.');
                if (dotIndex > 0 && dotIndex < nomeOriginal.length() - 1) {
                    extensao = nomeOriginal.substring(dotIndex);
                }
                String nomeArquivoUnico = System.currentTimeMillis() + "_img" + (i+1) + extensao;
                Path destination = diretorioDestino.resolve(nomeArquivoUnico);
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                caminhosRelativosSalvosNoBanco.set(i, IMAGES_COPY_DIRECTORY + File.separator + nomeArquivoUnico);
            }
        }
        return caminhosRelativosSalvosNoBanco;
    }

    private void limparCampos() {
        nomeField.setText("");
        valorField.setText("");
        descricaoArea.setText("");

        quantidadePField.setText("");
        quantidadeMField.setText("");
        quantidadeGField.setText("");
        quantidadeNum38Field.setText("");
        quantidadeNum39Field.setText("");
        quantidadeNum40Field.setText("");
        quantidadeNum41Field.setText("");
        quantidadeNum42Field.setText("");

        for(int i=0; i<3; i++) {
            imagePaths.set(i, null);
            if (statusLabelsImagens[i] != null) {
                 statusLabelsImagens[i].setText("(Nenhuma imagem selecionada)");
                 statusLabelsImagens[i].setToolTipText(null);
            }
        }
        tipoProdutoComboBox.setSelectedIndex(0);
        cardLayoutTamanhos.show(tamanhosContainerPanel, ROUPA_PANEL);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(TelaAdicionarProduto::new);
    }
}