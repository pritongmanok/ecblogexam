package com.epc.blog;

/**
 * Handler for: GET /blogs
 */

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FindBlogsHandler implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        //Request request = new Request(inputStream);

        String output = "return all output";

        Response response = new Response(outputStream);
        response.sendResponse(output);
    }



}

