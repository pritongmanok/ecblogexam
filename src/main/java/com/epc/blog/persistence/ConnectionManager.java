package com.epc.blog.persistence;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by 212396317 on 12/8/18.
 */
public class ConnectionManager {

    private static String url;
    private static String user;
    private static String password;

    private static ConnectionManager me = new ConnectionManager();

    private ConnectionManager() {
        try {
            Properties prop = new Properties();
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
            prop.load(is);
            is.close();

            Class.forName(prop.getProperty("db.driver"));
            url = prop.getProperty("db.url");
            user = prop.getProperty("db.user");
            password = prop.getProperty("db.password");
        } catch (Exception e) {
            //todo
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
