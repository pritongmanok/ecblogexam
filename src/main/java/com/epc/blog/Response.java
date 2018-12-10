package com.epc.blog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by Eddy Cruz on 12/8/18.
 */
public class Response {

    private OutputStream os;
    private static ObjectMapper mapper = new ObjectMapper();

    public Response(OutputStream os) {
        this.os = os;
    }

    public void sendResponse(String body, int status, String reason) throws IOException {
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("body", body);
        jsonNode.put("status", status);
        jsonNode.put("errorMessage", reason);

        OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
        writer.write(jsonNode.toString());
        writer.close();
    }

    public void sendResponse(JsonNode body, int status, String reason) throws IOException {
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("body", body);
        jsonNode.put("status", status);
        jsonNode.put("errorMessage", reason);

        OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
        writer.write(jsonNode.toString());
        writer.close();
    }

}
