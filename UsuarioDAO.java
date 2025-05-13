import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.NoSuchAlgorithmException;

public class UsuarioDAO {
    
    public boolean autenticar(String email, String senha) throws SQLException {
        boolean autenticado = false;
        try {
            String senhaHash = HashUtil.hashSenha(senha);  // Hash da senha informada
            String sql = "SELECT * FROM usuario WHERE email = ? AND senha = ?";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                stmt.setString(2, senhaHash);  // Compara com o hash da senha no banco

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        autenticado = true;
                    }
                }
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Erro ao gerar hash para autenticação: " + e.getMessage());
        }
        return autenticado;
    }

    public void cadastrarUsuario(String nome, String email, String senha) throws SQLException {
        try {
            String senhaHash = HashUtil.hashSenha(senha);  // Hash da senha antes de salvar
            String sql = "INSERT INTO usuario(nome, email, senha) VALUES (?,?,?)";
            
            try (Connection conexao = DBConnection.getConnection();
                 PreparedStatement stmt = conexao.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.setString(2, email);
                stmt.setString(3, senhaHash);  // Armazena a senha com hash

                stmt.executeUpdate();
                System.out.println("Usuário cadastrado com sucesso");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new SQLException("Erro ao gerar hash da senha", e);
        }
    }
}
