package org.abelsromero.box.sdk;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class Files {

    private static final Logger logger = LoggerFactory.getLogger(Files.class);

    private static final String API_URL = "https://api.box.com/2.0";

    public String upload(InputStream content, String filename, String folderId, String token) throws UnirestException {

        String fileInfo = (folderId == null || folderId.isEmpty()) ? "{\"name\":\"" + filename + "\"}}"
                : "{\"name\":\"" + filename + "\", \"parent\":{\"id\":\"" + folderId + "\"}}";

        HttpResponse<JsonNode> response = Unirest.post("https://upload.box.com/api/2.0/files/content")
                .header("Authorization:", "Bearer " + token)
                .field("attributes", fileInfo)
                .field("file", content, filename)
                .asJson();

        // check status to avoid message creation overhead
        if (response.getStatus() != 201) {
            String msg = "uploading " + filename + " into " + ((folderId == null || folderId.isEmpty() ? "root folder" : folderId));
            handleResponse(response, 201, msg);
        }

        return response.getBody().getObject().getJSONArray("entries").getJSONObject(0).getString("id");
    }

    public void delete(String fileId, String token) throws UnirestException {

        HttpResponse<JsonNode> response = Unirest.delete(API_URL + "/files/" + fileId)
                .header("Authorization:", "Bearer " + token)
                .asJson();

        handleResponse(response, 204, "deleting " + fileId);
    }

    public JSONObject getInfo(String fileId, String token) throws UnirestException {

        HttpResponse<JsonNode> response = Unirest.get(API_URL + "/files/" + fileId)
                .header("Authorization:", "Bearer " + token)
                .asJson();

        handleResponse(response, 200, "retrieving info for " + fileId);

        return response.getBody().getObject();
    }

    public Set<MetadataTemplate> getMetadata(String fileId, String token) throws UnirestException {

        HttpResponse<JsonNode> response = Unirest.get(API_URL + "/files/" + fileId + "/metadata")
                .header("Authorization:", "Bearer " + token)
                .asJson();

        handleResponse(response, 200, "retrieving info for " + fileId);

        JSONArray metadatas = response.getBody().getObject().getJSONArray("entries");
        Set<MetadataTemplate> res = new HashSet<>();
        for (int i = 0; i < metadatas.length(); i++) {
            res.add(process(metadatas.getJSONObject(i)));
        }
        return res;
    }

    private MetadataTemplate process(JSONObject o) {
        MetadataTemplate mt = new MetadataTemplate();
        mt.setType(o.getString("$template"));
        mt.setValues(o);
        return  mt;
    }

    private void handleResponse(HttpResponse<JsonNode> response, int expectedCode, String message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Response -> [code:" + response.getStatus() + ", message:" + response.getStatusText() + "]'");
            logger.debug(response.getBody().toString());
        }

        if (response.getStatus() != expectedCode) {
            throw new BoxException("Error " + message +
                    ". [code:'" + response.getStatus() + "', message:'" + response.getStatusText() + "']", response);
        }
    }


}