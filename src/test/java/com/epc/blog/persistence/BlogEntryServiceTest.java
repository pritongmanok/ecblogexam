package com.epc.blog.persistence;

import com.epc.blog.testutils.BaseFixture;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Eddy Cruz on 12/9/18.
 */
public class BlogEntryServiceTest extends BaseFixture {

    @Test
    public void testCreateBlogEntry() {

        // create author1
        String userName1 = "eddy-" + UUID.randomUUID().toString();
        String email1 = String.format("eddy%d@yahoo.com", (int) Math.random() * 10000);
        String password1 = "124";
        UserService userService = new UserService();
        Result<String> userResult1 = userService.createUser(userName1, email1, password1);
        String user1SessionId = userResult1.getResult();

        // create author2
        String userName2 = "mary-" + UUID.randomUUID().toString();
        String email2 = String.format("mary%d@yahoo.com", (int) Math.random() * 10000);
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

        // Method under test: blogEntryResult
        String blogEntryUri = "/a/b/e";
        BlogEntryService blogEntryService = new BlogEntryService();
        Result<Integer> blogEntryResult = blogEntryService.createBlogEntry(
                userResult2.getResult(), blogEntryUri, spaceId.toString());

        // Use another method to perform the verification (BlogEntryService.findUserBlogEntries)
        // Verify that the blog entry was created
        System.currentTimeMillis();
        Result<List<BlogEntry>> blogEntriesResult = blogEntryService.findUserBlogEntries(
                userResult2.getResult());
        List<BlogEntry> blogEntries = blogEntriesResult.getResult();
        Assert.assertTrue(blogEntries.size() == 1);
        Assert.assertEquals(blogEntries.get(0).getUri(), blogEntryUri);
    }
}
