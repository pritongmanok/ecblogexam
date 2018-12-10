package com.epc.blog;

/**
 * Handler for: PUT, URI: /blog/blogEntries/{blogEntryId}
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

public class ApproveBlogEntryHandler implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        Request request = new Request(inputStream);

        ObjectNode body = (ObjectNode)request.getRequestBody();
        String sessionId = body.get("sessionId").asText();
        boolean approval = body.get("approval").asBoolean();

        String blogEntryId = request.getUriPath("blogEntryId");

        Result<String> result = approveBlogEntry(sessionId, approval, blogEntryId);

        Response response = new Response(outputStream);
        response.sendResponse(result.getResult(), HttpStatus.SC_OK, null);
    }

    private Result approveBlogEntry(String sessionId, boolean approval, String blogEntryId) {
        BlogEntryService blogEntryService = new BlogEntryService();
        return blogEntryService.approveBlogEntry(sessionId, approval, blogEntryId);
    }

}

