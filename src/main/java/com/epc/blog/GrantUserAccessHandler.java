package com.epc.blog;

/**
 * Handler for: PATCH /blogs/spaces/{spaceId}
 */

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.epc.blog.persistence.BlogSpaceService;
import com.epc.blog.persistence.Result;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class GrantUserAccessHandler implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        Request request = new Request(inputStream);

        ObjectNode body = (ObjectNode)request.getRequestBody();
        String sessionId = body.get("sessionId").asText();

        List<String> otherAuthors = new ArrayList<>();
        ArrayNode arrayNode = (ArrayNode)body.get("userIds");
        for(int i=0; i<arrayNode.size(); i++) {
            String anotherUser = arrayNode.get(i).asText();
            otherAuthors.add(anotherUser);
        }

        String spaceId = request.getUriPath("spaceId");

        Result<String> result = grantAccess(sessionId, otherAuthors, spaceId);

        Response response = new Response(outputStream);
        response.sendResponse(result.getResult(), result.getStatus(), result.getErrorMessage());
    }

    private Result grantAccess(String sessionId, List<String> otherAuthors, String spaceId) {
        BlogSpaceService blogSpaceService = new BlogSpaceService();
        return blogSpaceService.grantAccess(sessionId, otherAuthors, spaceId);
    }

}

