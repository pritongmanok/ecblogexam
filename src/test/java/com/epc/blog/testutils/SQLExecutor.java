package com.epc.blog.testutils;

import com.epc.blog.persistence.ConnectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Executes a SQL file.
 */
public class SQLExecutor {

    public static void execute(String resourceName) throws SQLException, IOException {
        Connection con = ConnectionManager.getConnection();
        String[] sqls = readSqlStatements(resourceName);
        Statement statement = con.createStatement();
        Pattern whitespace = Pattern.compile("\\s+");
        for(String sql: sqls) {
            Matcher whiteSpaceMatcher = whitespace.matcher(sql);
            if(whiteSpaceMatcher.matches()) {
                continue;
            }
            System.out.println("Executing " + sql);
            statement.execute(sql);
        }
        con.commit();
        con.close();
    }

    public static String[] readSqlStatements(String resourceName) throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        while(true) {
            String line = br.readLine();
            if(line == null) {
                break;
            }
            if(line.startsWith("--")) {
                continue;
            }
            sb.append(line + " ");
        }
        return sb.toString().split(";");
    }

}
