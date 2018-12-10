package com.epc.blog.persistence;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eddy Cruz on 12/9/18.
 */
public class BlogEntryService {

    private static final Logger logger = LoggerFactory.getLogger(BlogEntryService.class);

    public Result<Integer> createBlogEntry(String sessionId, String uri, String spaceId) {

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = ConnectionManager.getConnection();
            UserSessionService userSessionService = new UserSessionService(con);
            String user = userSessionService.findUserBySessionId(sessionId);
            if (user == null) {
                return new Result(0, HttpStatus.SC_FORBIDDEN, "Invalid sessionId");
            }

            // check that the user has access to the space
            BlogSpaceService blogSpaceService = new BlogSpaceService();
            if(!blogSpaceService.canPublish(con, user, Integer.parseInt(spaceId))) {
                return new Result(0, HttpStatus.SC_FORBIDDEN,
                        "User cannot publish to this blog space");
            }

            ps = con.prepareStatement("insert into blog_entry(space_id, uri, approved, author) values(?,?,?,?)");
            ps.setInt(1, Integer.parseInt(spaceId));
            ps.setString(2, uri);
            ps.setBoolean(3, false); // blog entry is disabled by default
            ps.setString(4, user);
            ps.execute();
            ps.close();
            con.commit();

            // retrieve the blog entry id so it can be returned
            ps = con.prepareStatement("select id from blog_entry where space_id=? and uri=?");
            ps.setInt(1, Integer.parseInt(spaceId));
            ps.setString(2, uri);
            rs = ps.executeQuery();
            rs.next();

            return new Result(rs.getInt(1), HttpStatus.SC_OK, null);
        } catch (SQLException e) {
            logger.error(String.format("Error while creating a new blog entry; sessionId=%s, uri=%s, spaceId=%s",
                    sessionId, uri));
            return new Result(null, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error creating the blog space");

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

    public Result<List<BlogEntry>> findUserBlogEntries(String sessionId) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ConnectionManager.getConnection();

            UserSessionService userSessionService = new UserSessionService(con);
            String user = userSessionService.findUserBySessionId(sessionId);
            if (user == null) {
                return new Result(0, HttpStatus.SC_FORBIDDEN, "Invalid sessionId");
            }

            List<BlogEntry> blogEntries = new ArrayList<>();
            ps = con.prepareStatement("select id, space_id, uri, approved from blog_entry where author=?");
            ps.setString(1, user);
            rs = ps.executeQuery();
            while(rs.next()) {
                BlogEntry blogEntry = new BlogEntry();
                blogEntry.setId(rs.getInt(1));
                blogEntry.setSpaceId(rs.getInt(2));
                blogEntry.setUri(rs.getString(3));
                blogEntry.setApproved(rs.getBoolean(4));
                blogEntries.add(blogEntry);
            }
            return new Result(blogEntries, HttpStatus.SC_OK, null);
        } catch (SQLException e) {
            String errorMessage = String.format("Error querying for blog entries of sessionId=%s", sessionId);
            logger.error(errorMessage, e);
            return new Result(null, HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage);

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

    public Result approveBlogEntry(String sessionId, boolean approval, String blogEntryId) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ConnectionManager.getConnection();

            UserSessionService userSessionService = new UserSessionService(con);
            String user = userSessionService.findUserBySessionId(sessionId);
            if (user == null) {
                return new Result(0, HttpStatus.SC_FORBIDDEN, "Invalid sessionId");
            }

            ps = con.prepareStatement("update blog_entry set approved=? where id=?");
            ps.setBoolean(1, approval);
            ps.setInt(2, Integer.parseInt(blogEntryId));
            ps.executeQuery();

            return new Result("", HttpStatus.SC_OK, null);
        } catch (SQLException e) {
            String errorMessage = String.format("Error querying for blog entries of sessionId=%s", sessionId);
            logger.error(errorMessage, e);
            return new Result(null, HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage);

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
}
