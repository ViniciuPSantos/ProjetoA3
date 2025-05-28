package com.exemplo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TelaBazar extends JFrame {

    public TelaBazar() {
        setTitle("Página do Bazar");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 250); // Aumentei a altura para acomodar o novo botão
        setLocationRelativeTo(null);
        setLayout(new FlowLayout(FlowLayout.CENTER, 50, 50)); // Layout para centralizar os botões

        JButton adicionarProdutoButton = new JButton("Adicionar Produto");
        JButton editarProdutoButton = new JButton("Editar Produto");
        JButton pedidosButton = new JButton("Pedidos"); // Novo botão

        adicionarProdutoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TelaAdicionarProduto telaAdicionarProduto = new TelaAdicionarProduto();
                telaAdicionarProduto.setVisible(true);
            }
        });

        editarProdutoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TelaEditarProduto telaEditarProduto = new TelaEditarProduto();
                telaEditarProduto.setVisible(true);
            }
        });

        pedidosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TelaPedidos telaPedidos = new TelaPedidos();
                telaPedidos.setVisible(true);
            }
        });

        add(adicionarProdutoButton);
        add(editarProdutoButton);
        add(pedidosButton); // Adiciona o novo botão

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaBazar());
    }
}
