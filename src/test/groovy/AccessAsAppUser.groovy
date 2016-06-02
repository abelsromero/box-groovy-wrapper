/**
 * Created by ABEL.SALGADOROMERO on 02/06/2016.
 */
package com.box.sdk.example

import org.abelsromero.box.sdk.helpers.FileHelpers;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxUser;
import com.box.sdk.EncryptionAlgorithm;
import com.box.sdk.IAccessTokenCache;
import com.box.sdk.InMemoryLRUAccessTokenCache;
import com.box.sdk.JWTEncryptionPreferences;


public final class AccessAsAppUser {

    public static void main(String[] args) throws IOException {

        def config = new ConfigSlurper().parse(FileHelpers.getFile('config.groovy').toURI().toURL())

        final String CLIENT_ID = config.zecm.clientId;
        final String CLIENT_SECRET = config.zecm.secret;
        final String USER_ID = config.zecm.appUserId;
        final String PUBLIC_KEY_ID = config.zecm.clientId;
        final String PRIVATE_KEY_FILE = config.zecm.pemPrivateKeyLocation;
        final String PRIVATE_KEY_PASSWORD = config.zecm.pemPrivatekeyPass;

        final int MAX_DEPTH = 1;
        final int MAX_CACHE_ENTRIES = 100;

        // Turn off logging to prevent polluting the output.
        Logger.getLogger("com.box.sdk").setLevel(Level.OFF);

        String privateKey = new String(FileHelpers.getFile(PRIVATE_KEY_FILE).readBytes());

        JWTEncryptionPreferences encryptionPref = new JWTEncryptionPreferences();
        encryptionPref.setPublicKeyID(PUBLIC_KEY_ID);
        encryptionPref.setPrivateKey(privateKey);
        encryptionPref.setPrivateKeyPassword(PRIVATE_KEY_PASSWORD);
        encryptionPref.setEncryptionAlgorithm(EncryptionAlgorithm.RSA_SHA_256);

        //It is a best practice to use an access token cache to prevent unneeded requests to Box for access tokens.
        //For production applications it is recommended to use a distributed cache like Memcached or Redis, and to
        //implement IAccessTokenCache to store and retrieve access tokens appropriately for your environment.
        IAccessTokenCache accessTokenCache = new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);

        BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection.getAppUserConnection(USER_ID, CLIENT_ID,
                CLIENT_SECRET, encryptionPref, accessTokenCache);

        BoxUser.Info userInfo = BoxUser.getCurrentUser(api).getInfo();
        System.out.format("Welcome, %s!\n\n", userInfo.getName());

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        listFolder(rootFolder, 0);
    }

    private static void listFolder(BoxFolder folder, int depth) {
        for (BoxItem.Info itemInfo : folder) {
            String indent = "";
            for (int i = 0; i < depth; i++) {
                indent += "    ";
            }

            System.out.println(indent + itemInfo.getName());
            if (itemInfo instanceof BoxFolder.Info) {
                BoxFolder childFolder = (BoxFolder) itemInfo.getResource();
                if (depth < 1) {
                    listFolder(childFolder, depth + 1);
                }
            }
        }
    }
}