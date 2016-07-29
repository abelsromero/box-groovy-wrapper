package org.tests;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.abelsromero.box.sdk.BoxException;
import org.abelsromero.box.sdk.Files;
import org.abelsromero.box.sdk.MetadataTemplate;
import org.abelsromero.box.sdk.helpers.FileHelpers;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by ABEL.SALGADOROMERO on 08/06/2016.
 */
public class UniRestUpload {

    Logger logger = LoggerFactory.getLogger(UniRestUpload.class);


    public static void main(String[] args) throws FileNotFoundException, UnirestException {

        String token = "";
        String folderId = "";

        File f = FileHelpers.getFile("Test Document 1.docx");
        if (!f.exists())
            throw new RuntimeException("File not found");

        Files files = new Files();

        String fileId = files.upload(new FileInputStream(f), f.getName() + System.currentTimeMillis(), folderId, token);
        System.out.println("ID: " + fileId);

        JSONObject info = files.getInfo(fileId, token);
        System.out.println("File creating date: " + info.get("created_at"));

        //  files.delete("76279463573", token);
        files.delete(fileId, token);
        System.out.println("Deleted");
        try {
            files.getInfo(fileId, token);
        } catch (BoxException e) {
            System.out.println("Error captured: " + e.getStatus() + "-" + e.getStatusText());
        }

        for (MetadataTemplate mt: files.getMetadata("", token)) {
            System.out.println(mt.getType());
        }
    }


}