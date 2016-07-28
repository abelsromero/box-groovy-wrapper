package org.tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.abelsromero.box.sdk.helpers.FileHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by ABEL.SALGADOROMERO on 08/06/2016.
 */
public class UniRestUpload {

    Logger logger = LoggerFactory.getLogger(UniRestUpload.class);

    public String uploadFile(InputStream content, String filename, String folderId, String token) throws UnirestException {
        String fileInfo = "{\"name\":\"" + filename + "\", \"parent\":{\"id\":\"" + folderId + "\"}}";

        HttpResponse<JsonNode> response = Unirest.post("https://upload.box.com/api/2.0/files/content")
                .header("Authorization:", "Bearer " + token)
                .field("attributes", fileInfo)
                .field("file", content, filename)
                .asJson();

        if (response.getStatus() != 201) {
            throw new RuntimeException("Error while uploading '" + filename + "'. [code:'" + response.getStatus() + "', message:'" + response.getStatusText() + "']");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Response -> [code:" + response.getStatus() + ", message:" + response.getStatusText() + "]'");
            logger.debug(response.getRawBody().toString());
        }

        return response.getBody().getObject().getJSONArray("entries").getJSONObject(0).getString("id");
    }

    public static void main(String[] args) throws FileNotFoundException, UnirestException {

        String token = "";
        String folderId = "";

        File f = FileHelpers.getFile("Test Document 1.docx");
        if (!f.exists())
            throw new RuntimeException("File not found");

        UniRestUpload instance = new UniRestUpload();
        String value = instance.uploadFile(new FileInputStream(f), f.getName() + System.currentTimeMillis(), folderId, token);
        System.out.println("ID: " + value);
    }


}