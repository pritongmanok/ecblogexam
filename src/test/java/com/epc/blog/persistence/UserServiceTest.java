package com.epc.blog.persistence;

import com.epc.blog.testutils.BaseFixture;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static com.epc.blog.persistence.ConnectionManager.*;

/**
 * Created by Eddy Cruz on 12/9/18.
 */
public class UserServiceTest extends BaseFixture {

    @Test
    public void testCreateUser() throws Exception {
        String userName = "eddy-" + System.currentTimeMillis();
        String email = String.format("prit%d@yahoo.com", System.currentTimeMillis());
        String password = "123";
        UserService userService = new UserService();
        Result<String> result = userService.createUser(userName, email, password);

        Connection con = getConnection();
        PreparedStatement ps1 = con.prepareStatement("select count(*) from user where user_name=? and email=?");
        ps1.setString(1, userName);
        ps1.setString(2, email);
        ResultSet rs = ps1.executeQuery();
        rs.next();
        Assert.assertTrue(rs.getInt(1) > 0);

        PreparedStatement ps2 = con.prepareStatement("select session_id from user_session where user_name=?");
        ps2.setString(1, userName);
        ResultSet rs2 = ps2.executeQuery();
        rs2.next();
        Assert.assertEquals(result.getResult(), rs2.getString(1));

        Result<String> findSessionResult = userService.getSessionId(userName, password);
        Assert.assertEquals(result.getResult(), findSessionResult.getResult());


    }
}
