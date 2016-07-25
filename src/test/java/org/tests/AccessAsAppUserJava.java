package org.tests;

import com.box.sdk.*;
import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import org.abelsromero.box.sdk.helpers.FileHelpers;
import org.tests.model.AppData;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.abelsromero.box.sdk.helpers.FileHelpers.getFile;

/**
 * Example of accessing as APP USER to a box repository
 *
 * Created by ABEL.SALGADOROMERO on 02/06/2016.
 */
public final class AccessAsAppUserJava {

    public static void main(String[] args) throws IOException {

        ConfigObject config = new ConfigSlurper().parse(FileHelpers.getFile("config.groovy").toURI().toURL());

        ConfigObject proxy = (ConfigObject) config.get("proxy");

        // Note: auth only works when disables proxy at IDE and using this configuration
        System.setProperty("http.proxyHost", (String) proxy.get("host"));
        System.setProperty("http.proxyPort", (String) proxy.get("port"));
        System.setProperty("https.proxyHost", (String) proxy.get("host"));
        System.setProperty("https.proxyPort", (String) proxy.get("port"));

        String configKey = "APP_ID";
        ConfigObject appConfig = (ConfigObject) config.get(configKey);

        final String CLIENT_ID = (String) appConfig.get("clientId");
        final String CLIENT_SECRET = (String) appConfig.get("secret");
        final String USER_ID = (String) appConfig.get("appUserId");
        // Can be set to null if the public key is not available
        final String PUBLIC_KEY_ID = (String) appConfig.get("keyId");
        final String PRIVATE_KEY_FILE = (String) appConfig.get("pemPrivateKeyLocation");
        final String PRIVATE_KEY_PASSWORD = (String) appConfig.get("pemPrivatekeyPass");

        AppData appData = new AppData();
        appData.setClientId(CLIENT_ID);
        appData.setSecret(CLIENT_SECRET);
        appData.setPublicKeyId(PUBLIC_KEY_ID);
        appData.setPrivateKey(getFile(PRIVATE_KEY_FILE));
        appData.setPrivateKeyPassword(PRIVATE_KEY_PASSWORD);

        // Turn off logging to prevent polluting the output.
        Logger.getLogger("com.box.sdk").setLevel(Level.OFF);

        int totalMinutes = 90;
        int secondsWait = 2;
        int loops = (totalMinutes * 3600) / secondsWait;

        BoxDeveloperEditionAPIConnection api = getAppUserConnection(USER_ID, appData);
        BoxDeveloperEditionAPIConnection api2 = getAppUserConnection(USER_ID, appData);

        for (int i = 0; i < loops; i++) {
            BoxUser.Info userInfo = BoxUser.getCurrentUser(api).getInfo();
            System.out.format("Welcome, %s!\n", userInfo.getName());

            listFolder(BoxFolder.getRootFolder(api), 0, 1, false);
            listFolder(BoxFolder.getRootFolder(api2), 0, 1, false);

            protectedSleep(secondsWait * 1000);
            System.out.println();
        }
    }

    private static void listFolder(BoxFolder folder, int currentDepth, int maxDepth, boolean silent) {
        if (maxDepth == 0)
            return;

        System.out.println("Token: " + folder.getAPI().getAccessToken());

        for (BoxItem.Info itemInfo : folder) {
            String indent = "";
            for (int i = 0; i < currentDepth; i++) {
                indent += "  ";
            }

            if (!silent)
                System.out.println(new Date() + ":" + indent + itemInfo.getName());
            else
                System.out.print(".");

            if (itemInfo instanceof BoxFolder.Info) {
                BoxFolder childFolder = (BoxFolder) itemInfo.getResource();
                if (currentDepth < 1) {
                    listFolder(childFolder, currentDepth + 1, maxDepth - 1, silent);
                }
            }
        }
    }

    private static BoxDeveloperEditionAPIConnection getAppUserConnection(String appUserId, AppData app) {

        JWTEncryptionPreferences encryptionPref = new JWTEncryptionPreferences();
        // Not required for standard connection
        encryptionPref.setPublicKeyID(app.getPublicKeyId());
        encryptionPref.setPrivateKey(app.getPrivateKey());
        encryptionPref.setPrivateKeyPassword(app.getPrivateKeyPassword());
        encryptionPref.setEncryptionAlgorithm(EncryptionAlgorithm.RSA_SHA_256);

        //It is a best practice to use an access token cache to prevent unneeded requests to Box for access tokens.
        //For production applications it is recommended to use a distributed cache like Memcached or Redis, and to
        //implement IAccessTokenCache to store and retrieve access tokens appropriately for your environment.
        final int MAX_CACHE_ENTRIES = 100;

        IAccessTokenCache accessTokenCache = new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);

        return BoxDeveloperEditionAPIConnection.getAppUserConnection(
                appUserId, app.getClientId(), app.getSecret(), encryptionPref, accessTokenCache);
    }

    private static void protectedSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}