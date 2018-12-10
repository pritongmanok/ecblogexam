package com.epc.blog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Eddy Cruz on 12/8/18.
 */
public class Request {

    private JsonNode root;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public Request(InputStream is) throws IOException {
        root = objectMapper.readTree(is);
    }

    public JsonNode getRequestBody() throws IOException {
        String requestBody = root.get("requestBody").asText();
        return objectMapper.readTree(requestBody);
    }

    public String getUriPath(String key) {
        return root.get("pathParam." + key).asText();
    }
}
