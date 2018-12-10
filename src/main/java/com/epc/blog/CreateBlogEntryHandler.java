package com.epc.blog;

/**
 *
 * Handler for: POST /blogs/blogEntries
 */

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.epc.blog.persistence.BlogEntryService;
import com.epc.blog.persistence.Result;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CreateBlogEntryHandler implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        Request request = new Request(inputStream);

        ObjectNode body = (ObjectNode)request.getRequestBody();
        String sessionId = body.get("sessionId").asText();
        String uri = body.get("uri").asText();
        String spaceId = body.get("spaceId").asText();

        Result<String> result = createBlogEntry(sessionId, uri, spaceId);

        Response response = new Response(outputStream);
        response.sendResponse(result.getResult(), result.getStatus(), result.getErrorMessage());
    }

    /*  Returns the blogEntryId within the Result object */
    private Result createBlogEntry(String sessionId, String uri, String spaceId) {
        BlogEntryService blogEntryService = new BlogEntryService();
        return blogEntryService.createBlogEntry(sessionId, uri, spaceId);
    }

}

