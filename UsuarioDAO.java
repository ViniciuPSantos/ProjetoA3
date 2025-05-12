/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.proejtoa3;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class UsuarioDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/meu_banco";
    private static final String USUARIO = "root";
    private static final String SENHA = "senha";
    
    public boolean autenticar(String email, String senha){
        boolean autenticado = false;
        try{
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/meu_banco", "root", "senha");
            
            String sql = "SELECT * FROM usuario WHERE email = ? AND senha = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, senha);
            
            ResultSet rs = stmt.executeQuery();
            
            if(rs.next()){
                autenticado = true;
            }
            
            rs.close();
            stmt.close();
            conn.close();
        }catch (Exception e){
            e.printStackTrace();
            
            
        }
        
        return autenticado;
    }
    
    
    public void cadastrarUsuario(String nome,String email,String senha){
        String sql = "INSERT INTO usuario(nome, email, senha) VALUES (?,?,?)";
        try (Connection conexao = DriverManager .getConnection(URL, USUARIO, SENHA);
                PreparedStatement stmt = conexao.prepareStatement(sql)){
                    stmt.setString(1, nome);
                    stmt.setString(2, email);
                    stmt.setString(3, senha);
                    
                    stmt.executeUpdate();
                    System.out.println("Usuário cadastrado com sucesso");
                }catch (SQLException e){
                    System.out.println("Erro ao cadastrar usuário: "+e.getMessage());
                }
            
    }
    
    public static void main(String[] args) {
        UsuarioDAO dao = new UsuarioDAO();
        dao.cadastrarUsuario("Vinicius", "vinicius@gmail.com", "12345");
    }
}
