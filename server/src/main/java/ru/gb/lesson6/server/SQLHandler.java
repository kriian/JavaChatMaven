package ru.gb.lesson6.server;

import java.sql.*;

public class SQLHandler {
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB = "jdbc:mysql://localhost/lesson?useUnicode=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Telefon290787";
    private static Connection connection;
    private static Statement statement;

    public static void connect() {
        try {
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(DB, USER, PASSWORD);
            statement = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPassword(String login, String password) {
        try {
            ResultSet rs = statement.executeQuery("SELECT nickname FROM users WHERE login ='" + login + "' AND password = '" + password + "'");
            if (rs.next()) {
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean tryToRegister(String login, String password, String nickname) {
        try {
            statement.executeUpdate("INSERT INTO users (login, password, nickname) VALUES ('" + login + "','" + password + "','" + nickname + "')");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean isSuchNickname(String nickname) {
        try {
            ResultSet rs = statement.executeQuery("SELECT nickname FROM users WHERE nickname = '" + nickname + "'");
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void updateNickname(String newNick, String oldNick) {
        try {
            statement.executeUpdate("UPDATE users SET nickname = '"+newNick+"' WHERE nickname = '"+oldNick+"'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
