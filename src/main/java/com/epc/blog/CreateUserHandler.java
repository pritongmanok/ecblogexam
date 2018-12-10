package com.epc.blog;

/**
 * Creates a user.  Returns a password hash
 * This hash need to be passed to the other REST endpoints so that
 * the requests can be associated with the user that is logged in.
 *
 * Handler for: POST /blogs/users
 */

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.epc.blog.persistence.Result;
import com.epc.blog.persistence.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CreateUserHandler implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        Request request = new Request(inputStream);

        ObjectNode body = (ObjectNode)request.getRequestBody();
        String userName = body.get("username").asText();
        String email = body.get("email").asText();
        String password = body.get("password").asText();

        Result<String> result = createUser(userName, email, password);

        Response response = new Response(outputStream);
        response.sendResponse(result.getResult(), result.getStatus(), result.getErrorMessage());
    }

    // Returns sessionId
    private Result createUser(String userName, String email, String password) {
        UserService userService = new UserService();
        return userService.createUser(userName, email, password);
    }
}

