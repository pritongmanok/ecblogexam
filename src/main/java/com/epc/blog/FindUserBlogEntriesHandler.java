package com.epc.blog;

/**
 * Handler for: GET /blogs/blogEntries/sessionId=12345
 */

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.epc.blog.persistence.BlogEntry;
import com.epc.blog.persistence.BlogEntryService;
import com.epc.blog.persistence.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FindUserBlogEntriesHandler implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        Request request = new Request(inputStream);

        String sessionId = request.getUriPath("sessionId");

        Result<List<BlogEntry>> result = findUserBlogEntries(sessionId);

        Response response = new Response(outputStream);

        ObjectMapper objectMapper = new ObjectMapper();
        String entriesStr = objectMapper.writeValueAsString(result.getResult());
        JsonNode blogEntriesJson = objectMapper.readTree(entriesStr);

        response.sendResponse(blogEntriesJson, result.getStatus(), result.getErrorMessage());
    }

    // TODO:  This does not need to return anything.
    // Just return a string to check scaffolding.
    private Result<List<BlogEntry>> findUserBlogEntries(String sessionId) {
        List<BlogEntry> blogEntries = new ArrayList<>();
        BlogEntry blogEntry = new BlogEntry();
        blogEntry.setUri(sessionId + "-" + sessionId);
        blogEntry.setApproved(false);
        blogEntry.setSpaceId(1);
        blogEntries.add(blogEntry);
        return new Result(blogEntries, HttpStatus.SC_OK, null);
    }

    private Result<List<BlogEntry>> underDevelopmentfindUserBlogEntries(String sessionId) {
        BlogEntryService blogEntryService = new BlogEntryService();
        return blogEntryService.findUserBlogEntries(sessionId);
    }

}

