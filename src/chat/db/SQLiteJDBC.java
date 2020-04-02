package chat.db;

import chat.HashFunction;

import java.io.InputStream;
import java.sql.*;

public class SQLiteJDBC {
    private Connection connection;

    public SQLiteJDBC() {
        connection = openConnection();
        createUserTable();
    }

    private Connection openConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:server.db");
//            System.out.println("DB connection established!");
            return c;
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
            return null;
        }
    }

    private void createUserTable() {
        try {
            Statement stmt = connection.createStatement();
            String sql = "CREATE TABLE USER " +
                    "(ID              INT PRIMARY KEY     NOT NULL," +
                    " USERNAME        TEXT                NOT NULL, " +
                    " HASH            TEXT                NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();

            HashFunction hashFunction = new HashFunction();
            hashFunction.update("pass".getBytes());
            byte[] hash = hashFunction.digest();

            sql = "INSERT INTO USER (ID,USERNAME,HASH) " +
                    "VALUES (?, ?, ?);";


            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, 1);
            pstmt.setString(2, "admin");
            pstmt.setBytes(3, hash);
            pstmt.execute();

            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, 2);
            pstmt.setString(2, "amirabbas");
            pstmt.setBytes(3, hash);
            pstmt.execute();

            System.out.println("User table created successfully");
        } catch (Exception ignored) {
        }
    }

    public byte[] getUserHash(String username) {
        if (connection == null) {
            connection = openConnection();
        }
        try {


            String sql = "SELECT HASH FROM USER WHERE USERNAME = ?";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            byte[] hash = rs.getBytes("HASH");
            pstmt.close();
            return hash;
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        connection = null;
    }
}