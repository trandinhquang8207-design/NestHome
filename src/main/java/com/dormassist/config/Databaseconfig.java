package com.dormassist.config;

import java.io.*;
import java.sql.*;
import java.util.Properties;

public class Databaseconfig {

    private static final String CONFIG_FILE = "db.properties";
    private static String host = "localhost";
    private static String port = "1433";
    private static String dbName = "dormassist";
    private static String user = "sa";
    private static String pass = "";
    private static boolean configured = false;

    static { loadConfig(); }

    private static void loadConfig() {
        File f = new File(CONFIG_FILE);
        if (f.exists()) {
            Properties p = new Properties();
            try (FileInputStream fis = new FileInputStream(f)) {
                p.load(fis);
                host   = p.getProperty("db.host", host);
                port   = p.getProperty("db.port", port);
                dbName = p.getProperty("db.name", dbName);
                user   = p.getProperty("db.user", user);
                pass   = p.getProperty("db.password", pass);
                configured = true;
            } catch (IOException ignored) {}
        }
    }

    public static boolean isConfigured() { return configured; }

    public static void saveConfig(String h, String p, String db, String u, String pw) {
        host = h; port = p; dbName = db; user = u; pass = pw;
        Properties props = new Properties();
        props.setProperty("db.host", h); props.setProperty("db.port", p);
        props.setProperty("db.name", db); props.setProperty("db.user", u);
        props.setProperty("db.password", pw);
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "DormAssist DB Config");
            configured = true;
        } catch (IOException ignored) {}
    }

    private static String buildUrl() {
        return "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + dbName
                + ";encrypt=true;trustServerCertificate=true;";
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy Microsoft SQL Server JDBC Driver. Vui lòng kiểm tra lại mssql-jdbc.jar", e);
        }
        return DriverManager.getConnection(buildUrl(), user, pass);
    }

    public static boolean testConnection(String h, String p, String db, String u, String pw) {
        String url = "jdbc:sqlserver://" + h + ":" + p + ";databaseName=" + db
                + ";encrypt=true;trustServerCertificate=true;";
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (Connection c = DriverManager.getConnection(url, u, pw)) {
                return c.isValid(3);
            }
        } catch (Exception e) { return false; }
    }

    public static String getHost() { return host; }
    public static String getPort() { return port; }
    public static String getDbName() { return dbName; }
    public static String getUser() { return user; }
    public static String getPassword() { return pass; }
}