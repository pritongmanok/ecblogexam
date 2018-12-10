package com.epc.blog.persistence;

import java.sql.*;
import java.util.UUID;

/**
 * Created by Eddy Cruz on 12/9/18.
 */
public class UserSessionService {

    private Connection con;

    public UserSessionService(Connection con) {
        this.con = con;
    }

    public String createUserSession(String userName) throws SQLException {
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

    // A session can only last 1 day
    private static final long sessionExpireTimeInMillis = 86400000L; // todo externalize to properties file

    /**
     * Finds a sessionId for a user if one exists and the sessionId has not yet expired.
     * @param con
     * @param userName
     * @return
     * @throws SQLException
     */
    public String findUserSession(String userName) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("select session_id from user_session where user_name=? and create_date > ?");

            ps.setString(1, userName);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()-sessionExpireTimeInMillis));

            rs = ps.executeQuery();
            if(!rs.next()) {
                return null;
            }
            return rs.getString(1);
        } finally {
            if(rs != null) {
                rs.close();
            }
            if(ps != null) {
                ps.close();
            }
        }
    }

    public String findUserBySessionId(String sessionId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = con.prepareStatement("select user_name from user_session where session_id=? and create_date>?");
            ps.setString(1, sessionId);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()-sessionExpireTimeInMillis));
            rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getString(1);
            }
            else {
                return null;
            }
        } finally {
            if(rs != null) {
                rs.close();
            }
            if(ps != null) {
                ps.close();
            }
        }
    }

}
