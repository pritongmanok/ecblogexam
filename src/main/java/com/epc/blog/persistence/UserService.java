package com.epc.blog.persistence;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by Eddy Cruz on 12/9/18.
 */
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Creates a new user
     * @param userName
     * @param email
     * @param password
     * @return Returns a Result object which contains the sessionId which can be
     * used in making subsequent calls to the blog endpoints
     */
    public Result createUser(String userName, String email, String password) {
        Connection con = null;
        try {
            con = ConnectionManager.getConnection();
            if(!validateUserName(userName)) {
                return new Result(null, HttpStatus.SC_NOT_ACCEPTABLE,
                        "Invalid username");
            }
            if(!validateEmail(email)) {
                return new Result(null, HttpStatus.SC_NOT_ACCEPTABLE,
                        "Invalid email");
            }
            con = ConnectionManager.getConnection();
            if(doesUserExist(con, userName)) {
                return new Result(null, HttpStatus.SC_CONFLICT,
                        "Username already exist");
            }
            if(doesEmailExist(con, email)) {
                return new Result(null, HttpStatus.SC_CONFLICT,
                        "Email already exist");
            }
            String sessionId = insertUser(con, userName, email, password);
            con.commit();
            return new Result(sessionId, HttpStatus.SC_CREATED,
                    null);
        } catch (SQLException e) {
            // Log the error stacktrace, but do not let the actual error float up the stack.
            // The http/rest client won't care about the specific error.
            logger.error(String.format("Error while creating user=%s, email=%s", userName, email), e);
            return new Result(null, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Server error");
        } finally {
            if(con != null) {
                try {  con.close(); } catch (SQLException e) { }
            }
        }
    }

    /**
     * Creates a sessionId for the user.
     * @param userId
     * @param password Plaintext password of the user
     * @return
     */
    public Result getSessionId(String userName, String password) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String passwordHash = hashPassword(password);

            con = ConnectionManager.getConnection();
            ps = con.prepareStatement("select count(*) from user where user_name=? and password=?");

            ps.setString(1, userName);
            ps.setString(2, passwordHash);
            rs = ps.executeQuery();
            rs.next();
            if(rs.getInt(1) == 0) {
                // user is invalid
            }
            UserSessionService userSessionService = new UserSessionService(con);
            String sessionId = userSessionService.findUserSession(userName);
            if(sessionId == null) {
                userSessionService.createUserSession(userName);
                con.commit();
            }
            return new Result(sessionId, HttpStatus.SC_OK, null);
        } catch (SQLException e) {
            // Log the error stacktrace, but do not let the actual error float up the stack.
            // The http/rest client won't care about the specific error.
            logger.error(String.format("Error while creating user=%s", userName));
            return new Result(null, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Server error");
        } finally {
            if(rs != null) {
                try { rs.close(); } catch (SQLException e) { }
            }
            if(ps != null) {
                try { ps.close(); } catch (SQLException e) { }
            }
            if(con != null) {
                try { con.close(); } catch (SQLException e) { }
            }
        }
    }

    private String insertUser(Connection con, String userName, String email, String password) throws SQLException {
        PreparedStatement ps = null;
        try {
            String passwordHash = hashPassword(password); // store the password hash in the database
            ps = con.prepareStatement("insert into user(user_name, email, password) values (?,?,?)");
            ps.setString(1, userName);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.execute();

            UserSessionService userSessionService = new UserSessionService(con);
            String sessionId = userSessionService.createUserSession(userName);
            return sessionId;
        } finally {
            if(ps != null) {
                ps.close();
            }
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8));
            byte[] base64 = Base64.getEncoder().encode(encodedhash);
            return new String(base64, 0, base64.length);
        } catch (NoSuchAlgorithmException e) {
            // it won't actually go here.  SHA-256 is part of the JDK
        }
        // still, if SHA-256 is miraculously missing somehow, just get
        // the base64 of the original password
        byte[] base64 = Base64.getEncoder().encode(password.getBytes());
        return new String(base64, 0, base64.length);
    }

    public String createUserSession(Connection con, String userName) throws SQLException {
        PreparedStatement ps = null;
        try {
            String uuid = UUID.randomUUID().toString();
            ps = con.prepareStatement("insert into user_session(user_name, session_id, create_date, expired) values (?,?,?,?)");
            ps.setString(1, userName);
            ps.setString(2, uuid);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setBoolean(4, false);
            ps.execute();
            return uuid;
        } finally {
            if(ps != null) {
                ps.close();
            }
        }
    }

    // Though the database table will have a unique constraint, it's better for user experience
    // if the specific error is handled surgically.  The problem with letting the database
    // fire the SQLException is the reasons for failure are too many.  It's better to just check
    // the database for the existence of the user.
    public boolean doesUserExist(Connection con, String user) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("select count(*) from user where user_name=?");
            ps.setString(1, user);
            rs = ps.executeQuery();
            rs.next(); // there is always a result since it's a count statement
            int count = rs.getInt(1);
            return count > 0;
        } finally {
            if(rs != null) {
                rs.close();
            }
            if(ps != null) {
                ps.close();
            }
        }
    }

    // This checks if the email already exist.
    private boolean doesEmailExist(Connection con, String email) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("select count(*) from user where email=?");
            ps.setString(1, email);
            rs = ps.executeQuery();
            rs.next(); // there is always a result since it's a count statement
            int count = rs.getInt(1);
            return count > 0;
        } finally {
            if(rs != null) {
                rs.close();
            }
            if(ps != null) {
                ps.close();
            }
        }
    }

    private static boolean validateEmail(String email) {
        return Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
                Pattern.CASE_INSENSITIVE).matcher(email).matches();
    }

    private boolean validateUserName(String user) {
        if("".equals(user)) {
            return false;
        }
        return !Pattern.compile("\\s*").matcher(user).matches();
    }

}
