package com.epc.blog;

/**
 *
 * Handler for: POST /blogs/spaces
 */

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.epc.blog.persistence.BlogSpaceService;
import com.epc.blog.persistence.Result;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CreateBlogSpaceHandler implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        Request request = new Request(inputStream);

        ObjectNode body = (ObjectNode)request.getRequestBody();
        String sessionId = body.get("sessionId").asText();
        String uri = body.get("uri").asText();

        Result<String> result = createSpace(sessionId, uri);

        Response response = new Response(outputStream);
        response.sendResponse(result.getResult(), result.getStatus(), result.getErrorMessage());
    }

    // Returns sessionId
    private Result createSpace(String sessionId, String uri) {
        return new Result(sessionId + "::" + uri,HttpStatus.SC_OK, null);
    }

    private Result under_development_createSpace(String sessionId, String uri) {
        BlogSpaceService blogSpaceService = new BlogSpaceService();
        return blogSpaceService.createSpace(sessionId, uri);
    }

}

