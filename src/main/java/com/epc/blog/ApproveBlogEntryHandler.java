package com.epc.blog;

/**
 * Handler for: PUT, URI: /blog/blogEntries/{blogEntryId}
 */

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ApproveBlogEntryHandler implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        Request request = new Request(inputStream);

        ObjectNode body = (ObjectNode)request.getRequestBody();
        String sessionId = body.get("sessionId").asText();
        boolean approval = body.get("approval").asBoolean();

        String blogEntryId = request.getUriPath("blogEntryId");

        String xxx = approveBlogEntry(sessionId, approval, blogEntryId);

        Response response = new Response(outputStream);
        response.sendResponse(xxx);
    }

    // TODO:  This does not need to return anything.
    // Just return a string to check scaffolding.
    private String approveBlogEntry(String sessionId, boolean approval, String blogEntryId) {
        return sessionId + "::" + approval + "::" + blogEntryId;
    }



}

