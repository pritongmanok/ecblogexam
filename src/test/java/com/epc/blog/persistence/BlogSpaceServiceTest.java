package com.epc.blog.persistence;

import com.epc.blog.testutils.BaseFixture;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Created by Eddy Cruz on 12/9/18.
 */
public class BlogSpaceServiceTest extends BaseFixture {

    @Test
    public void testCreateSpace() throws Exception {
        String userName = "eddy-" + UUID.randomUUID().toString();
        String email = String.format("prit%d@yahoo.com", (int)Math.random()*10000);
        String password = "124";
        UserService userService = new UserService();
        Result<String> userResult = userService.createUser(userName, email, password);
        String sessionId = userResult.getResult();

        String blogUri = "/a/b/c";
        BlogSpaceService blogSpaceService = new BlogSpaceService();
        Result<Integer> blogSpaceResult = blogSpaceService.createSpace(sessionId, blogUri);
        int spaceId = blogSpaceResult.getResult();

        Connection con = ConnectionManager.getConnection();
        PreparedStatement ps = con.prepareStatement("select owner, uri from blog_space where id=?");
        ps.setInt(1, spaceId);
        ResultSet rs = ps.executeQuery();
        rs.next();
        Assert.assertEquals(userName, rs.getString(1));
        Assert.assertEquals(blogUri, rs.getString(2));

        rs.close();
        ps.close();
        con.close();
    }


    @Test
    public void testGrantAccess() throws Exception {
        // create author1
        String randomStr = UUID.randomUUID().toString().replaceAll("-", "");
        String userName1 = "eddy-" + UUID.randomUUID().toString();
        String email1 = String.format("eddy%s@yahoo.com", randomStr);
        String password1 = "124";
        UserService userService = new UserService();
        Result<String> userResult1 = userService.createUser(userName1, email1, password1);
        String user1SessionId = userResult1.getResult();

        // create author2
        String userName2 = "mary2-" + UUID.randomUUID().toString();
        String email2 = String.format("mary%s@yahoo.com", randomStr);
        String password2 = "124";
        Result<String> userResult2 = userService.createUser(userName2, email2, password2);

        // create a blogspace
        String blogUri = "/a/b/d";
        BlogSpaceService blogSpaceService = new BlogSpaceService();
        Result<Integer> blogSpaceResult = blogSpaceService.createSpace(user1SessionId, blogUri);
        Integer spaceId = blogSpaceResult.getResult();

        // Run the method under test
        List<String> otherAuthors = Arrays.asList(userName2);
        blogSpaceService.grantAccess(user1SessionId, otherAuthors, spaceId.toString());

        // Verify
        Connection con = ConnectionManager.getConnection();
        PreparedStatement ps = con.prepareStatement("select count(*) from contributors where user_name=? and space_id=?");
        ps.setString(1, userName2);
        ps.setInt(2, spaceId);
        ResultSet rs = ps.executeQuery();
        rs.next();
        Assert.assertTrue(rs.getInt(1) == 1);

        rs.close();
        ps.close();
        con.close();
    }

    @Test
    public void testFindAllBlogs() throws Exception {
        // create author1
        String randomStr = UUID.randomUUID().toString().replaceAll("-", "");
        String userName1 = "eddy-" + randomStr;
        String email1 = String.format("eddy%s@yahoo.com", randomStr);
        String password1 = "124";
        UserService userService = new UserService();
        Result<String> userResult1 = userService.createUser(userName1, email1, password1);
        String user1SessionId = userResult1.getResult();

        // create a blogspace
        String blogUri = "/a/b/d";
        BlogSpaceService blogSpaceService = new BlogSpaceService();
        Result<Integer> blogSpaceResult = blogSpaceService.createSpace(user1SessionId, blogUri);
        Integer spaceId = blogSpaceResult.getResult();

        // create blog entries
        BlogEntryService blogEntryService = new BlogEntryService();
        String blogEntryUri1 = "/1/2/3";
        Result<Integer> blogEntryResult1 = blogEntryService.createBlogEntry(user1SessionId, blogEntryUri1, spaceId.toString());
        String blogEntryUri2 = "/1/2/4";
        Result<Integer> blogEntryResult2 = blogEntryService.createBlogEntry(user1SessionId, blogEntryUri2, spaceId.toString());
        Set<Integer> expectedBlogEntryIds = new HashSet<>();
        expectedBlogEntryIds.add(blogEntryResult1.getResult());
        expectedBlogEntryIds.add(blogEntryResult2.getResult());

        // run method under test
        Result<List<BlogSpace>> blogSpacesResult = blogSpaceService.findAllBlogs();

        // Verify by first finding the blog space created above
        // find the blog space created above
        int size = -1;
        for(BlogSpace blogSpace: blogSpacesResult.getResult()) {
            if(blogSpace.getId() == spaceId) {
                size = blogSpace.getBlogEntries().size();
                for(BlogEntry blogEntry: blogSpace.getBlogEntries()) {
                    Assert.assertTrue(expectedBlogEntryIds.contains(blogEntry.getId()));
                }
            }
        }
        Assert.assertTrue(size == 2);
    }
}
