package com.epc.blog.persistence;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by 212396317 on 12/9/18.
 */
public class BlogSpaceService {

    private static final Logger logger = LoggerFactory.getLogger(BlogSpaceService.class);

    /**
     * Creates a blog space.  The Result object will contain the 'spaceId' of the newly
     * created space.
     * @param sessionId
     * @param uri
     * @return
     */
    public Result<Integer> createSpace(String sessionId, String uri) {
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

            ps = con.prepareStatement("insert into blog_space(owner, uri) values(?,?)");
            ps.setString(1, user);
            ps.setString(2, uri);
            ps.execute();
            ps.close();

            ps = con.prepareStatement("select id from blog_space where owner=? and uri=?");
            ps.setString(1, user);
            ps.setString(2, uri);
            rs = ps.executeQuery();
            rs.next();
            int spaceId = rs.getInt(1);
            return new Result(spaceId, HttpStatus.SC_CREATED, null);
        } catch (SQLException e) {
            logger.error(String.format("Error while creating blog space sessionId=%s, uri=%s", sessionId, uri));
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

    public Result grantAccess(String sessionId, List<String> otherAuthors, String spaceId) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ConnectionManager.getConnection();
            UserSessionService userSessionService = new UserSessionService(con);
            String user = userSessionService.findUserBySessionId(sessionId);
            if(user == null) {
                return new Result(0, HttpStatus.SC_FORBIDDEN, "Invalid sessionId");
            }

            // Validate whether other authors already exist.  Though foreign key constraint in the
            // database will perform the validation as well, the error message coming from
            // the database is not very surgical.
            for(String author: otherAuthors) {
                UserService userService = new UserService();
                if(userService.doesUserExist(con, author)) {
                    addBlogSpacePrivelege(con, spaceId, author);
                }
            }
            con.commit();
            return new Result(null, HttpStatus.SC_OK, null);
        } catch (Exception e) {
            String errorMessage = String.format("Error granting access to spaceId=%s, users=%s",
                    spaceId, otherAuthors.toString());
            logger.error(errorMessage, e);
            return new Result(0, HttpStatus.SC_CONFLICT, "Invalid sessionId");
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

    private void addBlogSpacePrivelege(Connection con, String spaceId, String userName) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("select count(*) from contributors where user_name=? and space_id=?");
            ps.setString(1, userName);
            ps.setInt(2, Integer.parseInt(spaceId));
            rs = ps.executeQuery();
            rs.next();
            if(rs.getInt(1) > 0) {
                return; // User already has privelege to publish in blog space so don't create a new record
            }
            ps.close();

            ps = con.prepareStatement("insert into contributors(user_name, space_id) values(?,?)");
            ps.setString(1, userName);
            ps.setInt(2, Integer.parseInt(spaceId));
            ps.execute();
        } finally {
            if(rs != null) {
                try { rs.close(); } catch (SQLException e) { }
            }
            if(ps != null) {
                try { ps.close(); } catch (SQLException e) { }
            }
        }
    }

    // Validate by checking that the user has the privelege of
    // publishing into the blog space by
    //  1.  Check if the user is the owner of the blog space
    //  2.  Check if the user is a contributor to the blog space
    public boolean canPublish(Connection con, String user, int blogSpaceId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = con.prepareStatement("select count(*) from blog_space where owner=? and id=?");
            ps.setString(1, user);
            ps.setInt(2, blogSpaceId);
            rs = ps.executeQuery();
            rs.next();
            if(rs.getInt(1) > 0) {
                return true;
            }

            rs.close();
            ps.close();
            ps = con.prepareStatement("select count(*) from contributors where user_name=? and space_id=?");
            ps.setString(1, user);
            ps.setInt(2, blogSpaceId);
            rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } finally {
            if(rs != null) {
                try { rs.close(); } catch (SQLException e) { }
            }
            if(ps != null) {
                try { ps.close(); } catch (SQLException e) { }
            }
        }
    }

    public Result<List<BlogSpace>> findAllBlogs() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = ConnectionManager.getConnection();

            ps = con.prepareStatement("select s.id, s.owner, s.uri, e.id, e.uri, e.approved from blog_space s, blog_entry e where s.id=e.space_id");
            rs = ps.executeQuery();
            Map<Integer, BlogSpace> blogSpaceMap = new HashMap<>();
            List<BlogSpace> blogSpaces = new ArrayList<>();
            while(rs.next()) {
                int spaceId = rs.getInt(1);
                String owner = rs.getString(2);
                String blogUri = rs.getString(3);
                int blogEntryId = rs.getInt(4);
                String blogEntryUri = rs.getString(5);
                boolean blogEntryApproval = rs.getBoolean(6);

                BlogSpace blogSpace = blogSpaceMap.get(spaceId);
                if(blogSpace == null) {
                    blogSpace = new BlogSpace();
                    blogSpace.setOwner(owner);
                    blogSpace.setId(spaceId);
                    blogSpace.setUri(blogUri);
                    blogSpace.setBlogEntries(new ArrayList<>());
                    blogSpaceMap.put(spaceId, blogSpace);
                    blogSpaces.add(blogSpace);
                }

                BlogEntry blogEntry = new BlogEntry();
                blogEntry.setSpaceId(spaceId);
                blogEntry.setApproved(blogEntryApproval);
                blogEntry.setUri(blogEntryUri);
                blogEntry.setId(blogEntryId);
                List<BlogEntry> blogEntries = blogSpace.getBlogEntries();
                blogEntries.add(blogEntry);
            }
            return new Result(blogSpaces, HttpStatus.SC_OK, null);
        } catch (Exception e) {
            String errorMessage = "Error querying blog spaces";
            logger.error(errorMessage, e);
            return new Result(0, HttpStatus.SC_CONFLICT, errorMessage);
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
