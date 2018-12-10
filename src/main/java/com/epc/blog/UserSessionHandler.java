package com.epc.blog;

/**
 * Receives password hash and userId as input.  Returns a 'security' hash.
 * This hash need to be passed to the other REST endpoints so that
 * the requests can be associated with the user that is logged in.
 *
 * Handler for: POST /blogs/users/{user123}
 */

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.epc.blog.persistence.Result;
import com.epc.blog.persistence.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpStatus;


import java.io.*;

public class UserSessionHandler implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        Request request = new Request(inputStream);

        ObjectNode body = (ObjectNode)request.getRequestBody();
        String password = body.get("password").asText();
        String userId = request.getUriPath("userId");

        Result<String> result = getSessionId(userId, password);

        Response response = new Response(outputStream);
        response.sendResponse(result.getResult(), result.getStatus(), result.getErrorMessage());
    }

    // Returns sessionId
    private Result getSessionId(String userId, String password) {
        UserService userService = new UserService();
        return userService.getSessionId(userId, password);
    }
}

