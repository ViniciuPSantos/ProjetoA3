package com.exemplo;
import com.exemplo.DBConnector; // 👈 Adicionado import do seu conector
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files; // 👈 Import para java.nio.file.Files
import java.nio.file.StandardCopyOption; // 👈 Import para java.nio.file.StandardCopyOption
import java.sql.Connection;
// DriverManager não é mais necessário diretamente aqui para obter a conexão
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TelaEditarProduto extends JFrame {

    private JTable produtosTable;
    private DefaultTableModel tableModel;
    private JButton selecionarImagem1Button;
    private JButton selecionarImagem2Button;
    private JButton selecionarImagem3Button;
    private JTextField quantidadePField;
    private JTextField quantidadeMField;
    private JTextField quantidadeGField;
    private JTextField valorField;
    private JTextArea descricaoArea;
    private JButton salvarAlteracoesButton;
    private JButton removerProdutoButton;

    private List<String> newImagePaths = new ArrayList<>();
    private int selectedProductId = -1;
    private String currentImagePath1 = null;
    private String currentImagePath2 = null;
    private String currentImagePath3 = null;

    // 👇 Removidas as constantes de conexão daqui
    // private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/projetoa3";
    // private static final String DB_USER = "root";
    // private static final String DB_PASSWORD = "";

    public TelaEditarProduto() {
        setTitle("Editar Produto");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // setLocationRelativeTo(null); // MAXIMIZED_BOTH já cuida do posicionamento
        setLayout(new BorderLayout());

        // Tabela de Produtos
        tableModel = new DefaultTableModel(new Object[]{"ID", "Valor", "Qtd. P", "Qtd. M", "Qtd. G", "Descrição", "Imagem 1", "Imagem 2", "Imagem 3"}, 0);
        produtosTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(produtosTable);
        add(tableScrollPane, BorderLayout.NORTH);

        // Painel de Edição
        JPanel edicaoPanel = new JPanel(new GridBagLayout());
        edicaoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        edicaoPanel.add(new JLabel("Imagem 1:"), gbc);
        gbc.gridx++;
        selecionarImagem1Button = new JButton("Selecionar");
        edicaoPanel.add(selecionarImagem1Button, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        edicaoPanel.add(new JLabel("Imagem 2:"), gbc);
        gbc.gridx++;
        selecionarImagem2Button = new JButton("Selecionar");
        edicaoPanel.add(selecionarImagem2Button, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        edicaoPanel.add(new JLabel("Imagem 3:"), gbc);
        gbc.gridx++;
        selecionarImagem3Button = new JButton("Selecionar");
        edicaoPanel.add(selecionarImagem3Button, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        edicaoPanel.add(new JLabel("Quantidade P:"), gbc);
        gbc.gridx++;
        quantidadePField = new JTextField(10);
        edicaoPanel.add(quantidadePField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        edicaoPanel.add(new JLabel("Quantidade M:"), gbc);
        gbc.gridx++;
        quantidadeMField = new JTextField(10);
        edicaoPanel.add(quantidadeMField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        edicaoPanel.add(new JLabel("Quantidade G:"), gbc);
        gbc.gridx++;
        quantidadeGField = new JTextField(10);
        edicaoPanel.add(quantidadeGField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        edicaoPanel.add(new JLabel("Valor:"), gbc);
        gbc.gridx++;
        valorField = new JTextField(10);
        edicaoPanel.add(valorField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        gbc.gridwidth = 2; // Span para o label da descrição
        edicaoPanel.add(new JLabel("Descrição:"), gbc);
        gbc.gridy++; // Próxima linha para o JTextArea
        descricaoArea = new JTextArea(5, 20);
        descricaoArea.setLineWrap(true);
        descricaoArea.setWrapStyleWord(true);
        JScrollPane descricaoScrollPane = new JScrollPane(descricaoArea); // Adiciona JScrollPane à JTextArea
        gbc.fill = GridBagConstraints.BOTH; // Permite que a área de texto expanda
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // Permite expansão vertical
        edicaoPanel.add(descricaoScrollPane, gbc);

        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Reseta fill para horizontal para os botões
        gbc.weighty = 0; // Reseta weighty

        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        salvarAlteracoesButton = new JButton("Salvar Alterações");
        removerProdutoButton = new JButton("Remover Produto");
        botoesPanel.add(salvarAlteracoesButton);
        botoesPanel.add(removerProdutoButton);
        edicaoPanel.add(botoesPanel, gbc);

        add(new JScrollPane(edicaoPanel), BorderLayout.CENTER); // Painel de edição também em um scrollpane

        carregarProdutos();

        selecionarImagem1Button.addActionListener(e -> selecionarNovaImagem(1));
        selecionarImagem2Button.addActionListener(e -> selecionarNovaImagem(2));
        selecionarImagem3Button.addActionListener(e -> selecionarNovaImagem(3));

        produtosTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = produtosTable.getSelectedRow();
                if (selectedRow != -1) {
                    selectedProductId = (int) tableModel.getValueAt(selectedRow, 0);
                    valorField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    quantidadePField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    quantidadeMField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                    quantidadeGField.setText(tableModel.getValueAt(selectedRow, 4).toString());
                    descricaoArea.setText(tableModel.getValueAt(selectedRow, 5).toString());
                    currentImagePath1 = (String) tableModel.getValueAt(selectedRow, 6);
                    currentImagePath2 = (String) tableModel.getValueAt(selectedRow, 7);
                    currentImagePath3 = (String) tableModel.getValueAt(selectedRow, 8);
                    newImagePaths.clear(); // Limpa caminhos de novas imagens ao selecionar um produto existente
                }
            }
        });

        salvarAlteracoesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedProductId != -1) {
                    salvarAlteracoesProduto();
                } else {
                    JOptionPane.showMessageDialog(TelaEditarProduto.this, "Selecione um produto para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        removerProdutoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedProductId != -1) {
                    removerProdutoDoBanco();
                } else {
                    JOptionPane.showMessageDialog(TelaEditarProduto.this, "Selecione um produto para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        setVisible(true);
    }

    private void carregarProdutos() {
        tableModel.setRowCount(0);
        DBConnector dbConnector = new DBConnector(); // 👈 Instanciando seu conector

        String sql = "SELECT id, valor, quantidade_p, quantidade_m, quantidade_g, descricao, imagens1_path, imagens2_path, imagens3_path FROM produtos";
        try (Connection conn = dbConnector.conectar(); // 👈 Usando o método conectar()
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) { // ResultSet também no try-with-resources

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getDouble("valor"),
                        rs.getInt("quantidade_p"),
                        rs.getInt("quantidade_m"),
                        rs.getInt("quantidade_g"),
                        rs.getString("descricao"),
                        rs.getString("imagens1_path"),
                        rs.getString("imagens2_path"),
                        rs.getString("imagens3_path")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar produtos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void selecionarNovaImagem(int imagemIndex) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Imagem " + imagemIndex);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            // Garante que a lista tenha tamanho suficiente, preenchendo com nulls se necessário
            while (newImagePaths.size() <= imagemIndex -1) { // Usa <= para garantir o índice
                newImagePaths.add(null);
            }
            newImagePaths.set(imagemIndex - 1, selectedFile.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Nova imagem " + imagemIndex + " selecionada: " + selectedFile.getName(), "Imagem Selecionada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void salvarAlteracoesProduto() {
        if (selectedProductId == -1) {
            JOptionPane.showMessageDialog(this, "Nenhum produto selecionado para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String quantidadePStr = quantidadePField.getText();
        String quantidadeMStr = quantidadeMField.getText();
        String quantidadeGStr = quantidadeGField.getText();
        String valorStr = valorField.getText();
        String descricao = descricaoArea.getText();

        try {
            int quantidadeP = Integer.parseInt(quantidadePStr);
            int quantidadeM = Integer.parseInt(quantidadeMStr);
            int quantidadeG = Integer.parseInt(quantidadeGStr);
            double valor = Double.parseDouble(valorStr);

            File diretorioImagens = new File("imagens");
            if (!diretorioImagens.exists()) {
                diretorioImagens.mkdirs();
            }

            String imagem1Path = salvarImagemSeSelecionada(0, currentImagePath1);
            String imagem2Path = salvarImagemSeSelecionada(1, currentImagePath2);
            String imagem3Path = salvarImagemSeSelecionada(2, currentImagePath3);

            DBConnector dbConnector = new DBConnector(); // 👈 Instanciando seu conector
            String sql = "UPDATE produtos SET valor = ?, quantidade_p = ?, quantidade_m = ?, quantidade_g = ?, descricao = ?, imagens1_path = ?, imagens2_path = ?, imagens3_path = ? WHERE id = ?";

            // 👇 Usando try-with-resources para Connection e PreparedStatement
            try (Connection conn = dbConnector.conectar();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setDouble(1, valor);
                pstmt.setInt(2, quantidadeP);
                pstmt.setInt(3, quantidadeM);
                pstmt.setInt(4, quantidadeG);
                pstmt.setString(5, descricao);
                pstmt.setString(6, imagem1Path);
                pstmt.setString(7, imagem2Path);
                pstmt.setString(8, imagem3Path);
                pstmt.setInt(9, selectedProductId);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Produto atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    carregarProdutos(); // Recarrega a lista de produtos
                    newImagePaths.clear(); // Limpa os caminhos das novas imagens após salvar
                    // Opcional: limpar campos ou deselecionar linha
                } else {
                    JOptionPane.showMessageDialog(this, "Falha ao atualizar o produto (nenhuma linha afetada).", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            } // conn e pstmt são fechados automaticamente aqui

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, insira valores numéricos válidos para quantidade e valor.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao acessar o banco de dados ao salvar: " + ex.getMessage(), "Erro SQL", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private String salvarImagemSeSelecionada(int index, String currentPath) {
        if (newImagePaths.size() > index && newImagePaths.get(index) != null) {
            File origem = new File(newImagePaths.get(index));
            String nomeArquivoBase = origem.getName();
            String extensao = "";
            int i = nomeArquivoBase.lastIndexOf('.');
            if (i > 0) {
                extensao = nomeArquivoBase.substring(i); // inclui o ponto, ex: .jpg
                nomeArquivoBase = nomeArquivoBase.substring(0, i);
            }
            // Garante um nome de arquivo único e sanitizado
            String nomeArquivo = System.currentTimeMillis() + "_" + nomeArquivoBase.replaceAll("[^a-zA-Z0-9.-]", "_") + extensao;
            File destino = new File("imagens", nomeArquivo);

            try {
                Files.copy(origem.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return "imagens/" + nomeArquivo; // Caminho relativo salvo no banco
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar imagem " + (index + 1) + ": " + e.getMessage(), "Erro de Arquivo", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return currentPath; // Retorna o caminho antigo se a cópia falhar
            }
        }
        return currentPath; // Retorna o caminho atual se nenhuma nova imagem foi selecionada para este slot
    }

    private void removerProdutoDoBanco() {
        if (selectedProductId == -1) {
            JOptionPane.showMessageDialog(this, "Nenhum produto selecionado para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja remover este produto?", "Confirmação", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            DBConnector dbConnector = new DBConnector(); // 👈 Instanciando seu conector
            String sql = "DELETE FROM produtos WHERE id = ?";

            // 👇 Usando try-with-resources para Connection e PreparedStatement
            try (Connection conn = dbConnector.conectar();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, selectedProductId);
                int rowsDeleted = pstmt.executeUpdate();

                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Produto removido com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    carregarProdutos(); // Recarrega a lista de produtos
                    limparCamposEdicao(); // Limpa os campos de edição
                    selectedProductId = -1; // Reseta o ID do produto selecionado
                    newImagePaths.clear();
                } else {
                    JOptionPane.showMessageDialog(this, "Falha ao remover o produto (produto não encontrado ou já removido).", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao acessar o banco de dados ao remover: " + ex.getMessage(), "Erro SQL", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    // Método auxiliar para limpar campos de edição
    private void limparCamposEdicao() {
        quantidadePField.setText("");
        quantidadeMField.setText("");
        quantidadeGField.setText("");
        valorField.setText("");
        descricaoArea.setText("");
        // Opcional: resetar os botões de imagem ou labels de caminho de imagem se você os tiver
        currentImagePath1 = null;
        currentImagePath2 = null;
        currentImagePath3 = null;
        produtosTable.clearSelection(); // Limpa a seleção da tabela
    }

    public static void main(String[] args) {
        // Para testes, é bom ter dados no banco ou tratar o caso de tabela vazia.
        SwingUtilities.invokeLater(() -> new TelaEditarProduto());
    }
}
