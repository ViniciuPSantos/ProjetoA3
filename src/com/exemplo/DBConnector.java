package com.exemplo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    private String URL = "jdbc:mysql://192.168.0.159:3306/projetoa3";
    private String USER = "root";
    private String PASSWORD = "password"; //substitua pela sua senha

    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
