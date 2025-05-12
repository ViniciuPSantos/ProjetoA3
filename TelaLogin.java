package com.mycompany.proejtoa3;
import javax.swing.*;
import java.awt.*;
public class TelaLogin extends JFrame {
    private JTextField campoEmail;
    private JPasswordField campoSenha;
    private JButton botaoEntrar;
    
    public TelaLogin(){
        setTitle("Login");
        setSize(300,180);
        setDefaultCloseOperatio(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel painel = new JPanel(new GridLayout(3, 2, 10, 10));
        painel.add(new JLabel("Email"));
        campoEmail =  new JTextField();
        painel.add(campoEmail);
        
        painel.add(new JLabel("Senha"));
        campoSenha = new JPasswordField();
        painel.add(campoSenha);
        
        botaoEntrar = new JButton("Entrar");
        painel.add(botaoEntrar);
        painel.add(new JLabel());
        
        add(painel);
        
        botaoEntrar.addActionListener(e -> {
            String email = campoEmail.getText();
            String senha = new String(campoSenha.getPassword());
            
            UsuarioDAO dao = new UsuarioDAO();
            if(dao.autenticar(email, senha)){
                JOptionPane.showMessageDialog(this, "Login bem-sucedido!");
            }else{
                JOptionPane.showMessageDialog(this, "Email ou senha incorretos.");
            }
        });
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaLogin().setVisible(true));
    }
}
