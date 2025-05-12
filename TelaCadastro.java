package com.mycompany.proejtoa3;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
public class TelaCadastro extends JFrame{
    private JTextField campoNome, campoEmail;
    private JPasswordField campoSenha;
    private JButton botaoCadastrar;
    
    public TelaCadastro(){
        setTitle("Cadastro de Usuário");
        setSize(300,250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel painel = new JPanel(new GridLayout (4, 2, 10, 10));
        painel.add(new JLabel("Nome"));
        campoNome = new JTextField();
        painel.add(campoNome);
        
        painel.add(new JLabel("Email"));
        campoEmail = new JTextField();
        painel.add(campoEmail);
        
        painel.add(new JLabel("Senha"));
        campoSenha = new JPasswordField();
        painel.add(campoSenha);
        
        botaoCadastrar = new JButton("Cadastrar");
        painel.add(botaoCadastrar);
        painel.add(new JLabel());
        
        add(painel);
        
        botaoCadastrar.addActionListener(e ->{
            String nome = campoNome.getText();
            String email = campoEmail.getText();
            String senha = new String(campoSenha.getPassword());
            
            UsuarioDAO dao = new UsuarioDAO();
            try{
                dao.cadastrarUsuario(nome, email, senha);
                JOptionPane.showMessageDialog(null, "Usuário cadastrado com sucesso!");
                dispose();//fecha a tela
                new TelaLogin().setVisible(true);//abre a tela de login apos o cadastro
            }catch (Exception ex){
                JOptionPane.showMessageDialog(this, "Erro: "+ ex.getMessage());
            }
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaCadastro().setVisible(true));
    }
}
