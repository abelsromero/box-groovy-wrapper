package org.abelsromero.box.sdk;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.collections.map.HashedMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class Files {

    private static final Logger logger = LoggerFactory.getLogger(Files.class);

    private static final String API_URL = "https://api.box.com/2.0";

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");

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

    public void addMetadataTemplate(String fileId, String scope, String template, Map<String, Object> values, String token) throws UnirestException {

        JSONObject jso = new JSONObject();
        for (Map.Entry<String, Object> v : values.entrySet()) {
            if (v.getValue() instanceof Date)
                jso.put(v.getKey(), DATE_FORMATTER.format(v.getValue()));
            else
                jso.put(v.getKey(), v.getValue().toString());
        }

        HttpResponse<JsonNode> response = Unirest.post(API_URL + "/files/" + fileId + "/metadata/" + scope + "/" + template)
                .header("Authorization:", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(jso.toString())
                .asJson();

        handleResponse(response, 201, "adding metadata of type " + template + " for " + fileId);
    }

    public void updateMetadataTemplate(String fileId, String scope, String template, Map<String, Object> values, String token) throws UnirestException {

        JSONArray jsonObjects = new JSONArray();
        for (Map.Entry<String, Object> v : values.entrySet()) {
            JSONObject jso = new JSONObject();
            jso.put("op", "replace");
            jso.put("path", "/" + v.getKey());
            if (v.getValue() instanceof Date)
                jso.put("value", DATE_FORMATTER.format(v.getValue()));
            else
                jso.put("value", v.getValue().toString());
            jsonObjects.put(jso);
        }

        HttpResponse<JsonNode> response = Unirest.put(API_URL + "/files/" + fileId + "/metadata/" + scope + "/" + template)
                .header("Authorization:", "Bearer " + token)
                .header("Content-Type", "application/json-patch+json")
                .body(jsonObjects.toString())
                .asJson();

        handleResponse(response, 200, "updating metadata of type " + template + " for " + fileId);
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
        return mt;
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

    public static void main(String[] args) throws UnirestException {

        String token = "";
        String fileId = "";

        Files file = new Files();
        System.out.println("Original metadata");
        System.out.println(file.getMetadata(fileId, token));

        Map<String, Object> values = new HashedMap() {{
            put("metadataKey1", "Number - " + new Date());
            put("metadataKey2_is_date", new Date());
        }};
        // If template already exists, it will fail
        file.addMetadataTemplate(fileId, "enterprise", "templateName", values, token);

        System.out.println("Metadata added");
        System.out.println(file.getMetadata(fileId, token));

        values.put("metadataKey1", "Number_2 - " + new Date());
        // template can be updated many times
        file.updateMetadataTemplate(fileId, "enterprise", "templateName", values, token);
    }

}