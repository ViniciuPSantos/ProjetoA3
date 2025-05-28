package com.exemplo;
import com.exemplo.DBConnector; // 👈 Adicionado import do seu conector
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
// ActionListener é usado implicitamente com a referência de método, mas pode ser bom ter o import explícito se preferir.
// import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
// DriverManager não é mais necessário diretamente aqui para obter a conexão
import java.sql.PreparedStatement;
import java.sql.SQLException;
// Statement não é usado diretamente, então o import pode ser removido se não houver outro uso.
// import java.sql.Statement;
// ResultSet não é usado diretamente, então o import pode ser removido se não houver outro uso.
// import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TelaAdicionarProduto extends JFrame {

    private JTextField nomeField;
    private JTextField quantidadePField;
    private JTextField quantidadeMField;
    private JTextField quantidadeGField;
    private JTextField valorField;
    private JTextArea descricaoArea;
    private JButton salvarProdutoButton;
    private List<String> imagePaths; // Mantém os caminhos ABSOLUTOS dos arquivos selecionados pelo JFileChooser
    private JButton adicionarImagem1Button;
    private JButton adicionarImagem2Button;
    private JButton adicionarImagem3Button;
    private JLabel[] statusLabelsImagens; // Labels para mostrar o nome do arquivo selecionado

    // 👇 Removidas as constantes de conexão daqui
    // private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/projetoa3";
    // private static final String DB_USER = "root";
    // private static final String DB_PASSWORD = "";

    // Pasta onde as imagens serão copiadas (relativo à execução do JAR/IDE)
    private static final String IMAGES_COPY_DIRECTORY = "imagens_produtos_copiados";

    public TelaAdicionarProduto() {
        setTitle("Adicionar Novo Produto");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 550); // Aumentado um pouco para melhor layout
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10)); // Adiciona espaçamento entre componentes do BorderLayout
        imagePaths = new ArrayList<>(3); // Inicializa com capacidade 3
        for(int i=0; i<3; i++) imagePaths.add(null); // Preenche com nulls para facilitar o set(index, value)

        statusLabelsImagens = new JLabel[3];

        // Painel para seleção de imagens
        JPanel imageSelectionPanel = new JPanel(new GridLayout(1, 3, 10, 0)); // 1 linha, 3 colunas para botões
        imageSelectionPanel.setBorder(BorderFactory.createTitledBorder("Imagens do Produto"));

        adicionarImagem1Button = new JButton("Selecionar Imagem 1");
        adicionarImagem2Button = new JButton("Selecionar Imagem 2");
        adicionarImagem3Button = new JButton("Selecionar Imagem 3");

        JPanel panelImg1 = criarPainelSelecaoImagem(adicionarImagem1Button, 0);
        JPanel panelImg2 = criarPainelSelecaoImagem(adicionarImagem2Button, 1);
        JPanel panelImg3 = criarPainelSelecaoImagem(adicionarImagem3Button, 2);

        imageSelectionPanel.add(panelImg1);
        imageSelectionPanel.add(panelImg2);
        imageSelectionPanel.add(panelImg3);

        add(imageSelectionPanel, BorderLayout.NORTH);


        // Campos de entrada
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Nome do Produto:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; // Permite que o campo de texto expanda
        nomeField = new JTextField(20);
        inputPanel.add(nomeField, gbc);
        gbc.weightx = 0; // Reseta

        gbc.gridx = 0; gbc.gridy++;
        inputPanel.add(new JLabel("Quantidade (P):"), gbc);
        gbc.gridx = 1;
        quantidadePField = new JTextField(5);
        inputPanel.add(quantidadePField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        inputPanel.add(new JLabel("Quantidade (M):"), gbc);
        gbc.gridx = 1;
        quantidadeMField = new JTextField(5);
        inputPanel.add(quantidadeMField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        inputPanel.add(new JLabel("Quantidade (G):"), gbc);
        gbc.gridx = 1;
        quantidadeGField = new JTextField(5);
        inputPanel.add(quantidadeGField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        inputPanel.add(new JLabel("Valor (R$):"), gbc);
        gbc.gridx = 1;
        valorField = new JTextField(10);
        inputPanel.add(valorField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Alinha o label "Descrição" ao topo
        inputPanel.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1; gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH; // Permite expansão em ambas as direções
        gbc.weighty = 1.0; // Permite que a área de texto expanda verticalmente
        descricaoArea = new JTextArea(5, 20);
        descricaoArea.setLineWrap(true);
        descricaoArea.setWrapStyleWord(true);
        JScrollPane descricaoScrollPane = new JScrollPane(descricaoArea);
        inputPanel.add(descricaoScrollPane, gbc);
        gbc.weighty = 0; // Reseta
        gbc.fill = GridBagConstraints.HORIZONTAL; // Reseta

        add(new JScrollPane(inputPanel), BorderLayout.CENTER); // Adiciona scroll ao painel principal de inputs

        // Botão de salvar
        salvarProdutoButton = new JButton("Salvar Produto");
        salvarProdutoButton.setFont(new Font("Arial", Font.BOLD, 14));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Centraliza o botão
        bottomPanel.setBorder(new EmptyBorder(10,0,10,0));
        bottomPanel.add(salvarProdutoButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Ações para selecionar imagens
        adicionarImagem1Button.addActionListener(e -> selecionarImagem(0)); // índice 0 para imagem 1
        adicionarImagem2Button.addActionListener(e -> selecionarImagem(1)); // índice 1 para imagem 2
        adicionarImagem3Button.addActionListener(e -> selecionarImagem(2)); // índice 2 para imagem 3

        // Ação para salvar no banco
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


    private void selecionarImagem(int imagemIndex) { // usa índice baseado em 0
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Imagem " + (imagemIndex + 1));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagens", "jpg", "jpeg", "png", "gif"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            imagePaths.set(imagemIndex, selectedFile.getAbsolutePath()); // Atualiza o caminho na lista
            statusLabelsImagens[imagemIndex].setText(selectedFile.getName());
            statusLabelsImagens[imagemIndex].setToolTipText(selectedFile.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Imagem " + (imagemIndex + 1) + " selecionada: " + selectedFile.getName(),
                    "Imagem Selecionada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void salvarProdutoNoBanco(ActionEvent e) {
        String nome = nomeField.getText().trim();
        String quantidadePStr = quantidadePField.getText().trim();
        String quantidadeMStr = quantidadeMField.getText().trim();
        String quantidadeGStr = quantidadeGField.getText().trim();
        String valorStr = valorField.getText().trim();
        String descricao = descricaoArea.getText().trim();

        // Verifica se pelo menos uma imagem foi selecionada
        boolean algumaImagemSelecionada = false;
        for (String path : imagePaths) {
            if (path != null && !path.isEmpty()) {
                algumaImagemSelecionada = true;
                break;
            }
        }

        if (nome.isEmpty() || quantidadePStr.isEmpty() || quantidadeMStr.isEmpty() ||
                quantidadeGStr.isEmpty() || valorStr.isEmpty() || descricao.isEmpty() ||
                !algumaImagemSelecionada) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, preencha todos os campos de texto e selecione pelo menos a Imagem 1.",
                    "Campos Obrigatórios", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int quantidadeP = Integer.parseInt(quantidadePStr);
            int quantidadeM = Integer.parseInt(quantidadeMStr);
            int quantidadeG = Integer.parseInt(quantidadeGStr);
            double valor = Double.parseDouble(valorStr.replace(",", ".")); // Trata vírgula como decimal

            if (quantidadeP < 0 || quantidadeM < 0 || quantidadeG < 0 || valor < 0) {
                 JOptionPane.showMessageDialog(this,
                        "Quantidades e valor não podem ser negativos.",
                        "Valores Inválidos", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Copiar imagens e obter os caminhos relativos para o banco
            List<String> caminhosRelativosSalvos = copiarImagensParaDiretorio();

            DBConnector dbConnector = new DBConnector(); // 👈 Instanciando seu conector
            String sql = """
                    INSERT INTO produtos 
                    (nome, descricao, valor, quantidade_p, quantidade_m, quantidade_g, 
                     imagens1_path, imagens2_path, imagens3_path)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """; // Text block para SQL é mais legível

            // 👇 Usando try-with-resources para Connection e PreparedStatement
            try (Connection conn = dbConnector.conectar();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, nome);
                pstmt.setString(2, descricao);
                pstmt.setDouble(3, valor);
                pstmt.setInt(4, quantidadeP);
                pstmt.setInt(5, quantidadeM);
                pstmt.setInt(6, quantidadeG);

                // Define os caminhos das imagens, usando null se não houver imagem para o slot
                pstmt.setString(7, caminhosRelativosSalvos.size() > 0 && caminhosRelativosSalvos.get(0) != null ? caminhosRelativosSalvos.get(0) : null);
                pstmt.setString(8, caminhosRelativosSalvos.size() > 1 && caminhosRelativosSalvos.get(1) != null ? caminhosRelativosSalvos.get(1) : null);
                pstmt.setString(9, caminhosRelativosSalvos.size() > 2 && caminhosRelativosSalvos.get(2) != null ? caminhosRelativosSalvos.get(2) : null);


                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Produto '" + nome + "' salvo com sucesso!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    limparCampos();
                } else {
                    JOptionPane.showMessageDialog(this, "Falha ao salvar o produto. Nenhuma linha foi afetada no banco.",
                            "Erro Inesperado", JOptionPane.ERROR_MESSAGE);
                }
            } // conn e pstmt são fechados automaticamente aqui

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, insira valores numéricos válidos para quantidade e valor. Use ponto para decimais.",
                    "Erro de Formato Numérico", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao acessar o banco de dados: " + ex.getMessage(),
                    "Erro SQL", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao copiar imagens para o diretório: " + ex.getMessage(),
                    "Erro de Arquivo", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private List<String> copiarImagensParaDiretorio() throws IOException {
        List<String> caminhosRelativosSalvosNoBanco = new ArrayList<>(3);
         for(int i=0; i<3; i++) caminhosRelativosSalvosNoBanco.add(null); // Inicializa com nulls

        Path diretorioDestino = Paths.get(IMAGES_COPY_DIRECTORY);

        if (!Files.exists(diretorioDestino)) {
            Files.createDirectories(diretorioDestino);
        }

        for (int i = 0; i < imagePaths.size(); i++) {
            String sourcePathStr = imagePaths.get(i);
            if (sourcePathStr != null && !sourcePathStr.isEmpty()) {
                Path source = Paths.get(sourcePathStr);
                // Cria um nome de arquivo único para evitar sobrescrever e colisões
                String nomeOriginal = source.getFileName().toString();
                String extensao = "";
                int dotIndex = nomeOriginal.lastIndexOf('.');
                if (dotIndex > 0 && dotIndex < nomeOriginal.length() - 1) {
                    extensao = nomeOriginal.substring(dotIndex); // .jpg, .png
                }
                String nomeArquivoUnico = System.currentTimeMillis() + "_img" + (i+1) + extensao;
                Path destination = diretorioDestino.resolve(nomeArquivoUnico);

                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

                // O caminho salvo no banco deve ser relativo à forma como o servidor web/aplicação acessará
                // Se IMAGES_COPY_DIRECTORY for a raiz de onde o servidor web serve as imagens, então só o nome do arquivo.
                // Se for um subdiretório, inclua-o.
                // Exemplo: "imagens_produtos_copiados/nome_unico.jpg"
                caminhosRelativosSalvosNoBanco.set(i, IMAGES_COPY_DIRECTORY + File.separator + nomeArquivoUnico);
            }
        }
        return caminhosRelativosSalvosNoBanco;
    }

    private void limparCampos() {
        nomeField.setText("");
        quantidadePField.setText("");
        quantidadeMField.setText("");
        quantidadeGField.setText("");
        valorField.setText("");
        descricaoArea.setText("");
        for(int i=0; i<3; i++) {
            imagePaths.set(i, null);
            if (statusLabelsImagens[i] != null) {
                 statusLabelsImagens[i].setText("(Nenhuma imagem selecionada)");
                 statusLabelsImagens[i].setToolTipText(null);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TelaAdicionarProduto::new);
    }
}
