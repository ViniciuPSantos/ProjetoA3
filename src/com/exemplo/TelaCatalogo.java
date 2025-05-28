package com.exemplo;
import com.exemplo.DBConnector; // 👈 Adicionado import do seu conector
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
// DriverManager não é mais necessário diretamente aqui para obter a conexão
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TelaCatalogo extends JFrame {

    private JPanel produtosPanel;
    private JScrollPane scrollPane;
    private List<ProdutoCatalogo> listaDeProdutos;
    private JButton carrinhoButton;
    private JButton pedidosButton;

    // 👇 Removidas as constantes de conexão daqui
    // private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/projetoa3";
    // private static final String DB_USER = "root";
    // private static final String DB_PASSWORD = "";

    public TelaCatalogo() {
        setTitle("Catálogo de Produtos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // DISPOSE_ON_CLOSE é geralmente melhor para telas secundárias
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Painel do topo com botões do carrinho e pedidos
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        pedidosButton = new JButton("Meus Pedidos");
        pedidosButton.addActionListener(e -> {
            // Verifica se o usuário está logado antes de abrir a tela de pedidos
            if (SessaoUsuario.getInstance().isUsuarioLogado()) {
                TelaPedidosCliente telaPedidos = new TelaPedidosCliente();
                telaPedidos.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você precisa estar logado para ver seus pedidos.",
                        "Acesso Negado",
                        JOptionPane.WARNING_MESSAGE);
                // Opcional: abrir tela de login
                        new TelaLogin().setVisible(true);
            }
        });
        topPanel.add(pedidosButton);

        carrinhoButton = new JButton("Carrinho");
        carrinhoButton.addActionListener(e -> {
            TelaCarrinho telaCarrinho = new TelaCarrinho();
            telaCarrinho.setVisible(true);
        });
        topPanel.add(carrinhoButton);

        add(topPanel, BorderLayout.NORTH);

        produtosPanel = new JPanel();
        produtosPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15)); // Ou GridLayout para mais controle
        produtosPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        scrollPane = new JScrollPane(produtosPanel);
        add(scrollPane, BorderLayout.CENTER);

        listaDeProdutos = new ArrayList<>();
        carregarProdutosDoBanco();
        exibirProdutos();

        setVisible(true);
    }

    private void carregarProdutosDoBanco() {
        String sql = "SELECT id, nome, valor, imagens1_path FROM produtos"; // Idealmente, apenas produtos ativos/visíveis
        DBConnector dbConnector = new DBConnector(); // 👈 Instanciando seu conector

        // O try-with-resources já estava bem estruturado aqui
        try (Connection conn = dbConnector.conectar(); // 👈 Usando o método conectar()
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            listaDeProdutos.clear(); // Limpa a lista antes de carregar para evitar duplicatas se chamado novamente
            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                double valor = rs.getDouble("valor");
                String imagemPath = rs.getString("imagens1_path");
                listaDeProdutos.add(new ProdutoCatalogo(id, nome, valor, imagemPath));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar produtos: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void exibirProdutos() {
        produtosPanel.removeAll();
        for (ProdutoCatalogo produto : listaDeProdutos) {
            JPanel produtoPanel = new JPanel();
            produtoPanel.setLayout(new BoxLayout(produtoPanel, BoxLayout.Y_AXIS));
            produtoPanel.setPreferredSize(new Dimension(180, 270)); // Ajuste conforme necessário
            produtoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10,10,10,10) // Adiciona padding interno
            ));


            JLabel nomeLabel = new JLabel("<html><body style='width: 130px; text-align:center;'>" + produto.getNome() + "</body></html>"); // Quebra de linha e centralização
            nomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            nomeLabel.setFont(new Font("Arial", Font.BOLD, 14));


            ImageIcon imageIcon = carregarImagem(produto.getImagemPath());
            JLabel imagemLabel = new JLabel(imageIcon);
            imagemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imagemLabel.setPreferredSize(new Dimension(150,150)); // Define tamanho preferido para a imagem

            JLabel valorLabel = new JLabel("R$ " + String.format("%.2f", produto.getValor()));
            valorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            valorLabel.setFont(new Font("Arial", Font.PLAIN, 13));

            JButton adicionarCarrinhoButton = new JButton("Adicionar ao Carrinho");
            adicionarCarrinhoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            adicionarCarrinhoButton.setMargin(new Insets(5,5,5,5));
            adicionarCarrinhoButton.addActionListener(e -> {
                // Verifica se o usuário está logado antes de adicionar ao carrinho/abrir seleção de tamanho
                if (SessaoUsuario.getInstance().isUsuarioLogado()) {
                    TelaSelecaoTamanho telaSelecao = new TelaSelecaoTamanho(
                            produto.getId(),
                            produto.getNome(),
                            produto.getValor()
                    );
                    telaSelecao.setVisible(true);
                } else {
                     JOptionPane.showMessageDialog(this,
                        "Você precisa estar logado para adicionar itens ao carrinho.",
                        "Acesso Negado",
                        JOptionPane.WARNING_MESSAGE);
                    // Opcional: abrir tela de login
                    // new TelaLogin().setVisible(true);
                }
            });

            produtoPanel.add(nomeLabel);
            produtoPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Espaço
            produtoPanel.add(imagemLabel);
            produtoPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Espaço
            produtoPanel.add(valorLabel);
            produtoPanel.add(Box.createVerticalStrut(10));
            produtoPanel.add(adicionarCarrinhoButton);

            produtosPanel.add(produtoPanel);
        }
        produtosPanel.revalidate();
        produtosPanel.repaint();
    }

    private ImageIcon carregarImagem(String caminhoRelativo) {
        ImageIcon icon = null;
        if (caminhoRelativo != null && !caminhoRelativo.isEmpty()) {
            File imgFile = new File(caminhoRelativo);
            if (imgFile.exists() && !imgFile.isDirectory()) {
                try {
                    Image image = new ImageIcon(imgFile.toURI().toURL()).getImage().getScaledInstance(
                        150, 150, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(image);
                } catch (Exception e) {
                     System.err.println("Erro ao carregar imagem do arquivo: " + caminhoRelativo + " - " + e.getMessage());
                }
            } else {
                System.err.println("Arquivo de imagem não encontrado ou é um diretório: " + caminhoRelativo);
            }
        }

        // Se não encontrar ou houver erro, usa imagem padrão
        if (icon == null) {
            try {
                // Tenta carregar como recurso (geralmente de dentro de um JAR ou do classpath)
                Image imagemPadrao = new ImageIcon(getClass().getResource("/imagens/no_image.png"))
                        .getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                icon = new ImageIcon(imagemPadrao);
            } catch (Exception e) {
                System.err.println("Imagem padrão '/imagens/no_image.png' não encontrada como recurso. " + e.getMessage());
                // Cria um placeholder vazio se tudo falhar
                BufferedImage placeholder = new BufferedImage(150, 150, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = placeholder.createGraphics();
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(0, 0, 150, 150);
                g.setColor(Color.DARK_GRAY);
                g.drawString("No Image", 50, 75);
                g.dispose();
                icon = new ImageIcon(placeholder);
            }
        }
        return icon;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaCatalogo());
    }
}

// A classe ProdutoCatalogo permanece a mesma
class ProdutoCatalogo {
    private int id;
    private String nome;
    private double valor;
    private String imagemPath;

    public ProdutoCatalogo(int id, String nome, double valor, String imagemPath) {
        this.id = id;
        this.nome = nome;
        this.valor = valor;
        this.imagemPath = imagemPath;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public double getValor() { return valor; }
    public String getImagemPath() { return imagemPath; }
}