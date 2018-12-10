package com.epc.blog;

/**
 * Handler for: GET /blogs
 */

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.epc.blog.persistence.BlogSpace;
import com.epc.blog.persistence.BlogSpaceService;
import com.epc.blog.persistence.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class FindBlogsHandler implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        Result<List<BlogSpace>> result = findAllBlogs();

        List<BlogSpace> blogSpaces = result.getResult();
        ObjectMapper mapper = new ObjectMapper();
        String blogSpacesStr = mapper.writeValueAsString(blogSpaces);
        JsonNode blogNode = mapper.readTree(blogSpacesStr);

        Response response = new Response(outputStream);
        response.sendResponse(blogNode, HttpStatus.SC_OK, result.getErrorMessage());
    }

    private Result<List<BlogSpace>> findAllBlogs() {
        BlogSpaceService blogSpaceService = new BlogSpaceService();
        return blogSpaceService.findAllBlogs();
    }

}

