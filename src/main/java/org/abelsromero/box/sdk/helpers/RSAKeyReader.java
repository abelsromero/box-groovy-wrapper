package org.abelsromero.box.sdk.helpers;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Created by ABEL.SALGADOROMERO on 20/05/2016.
 */
public class RSAKeyReader {

    private static final String KEY_TYPE = "RSA";

    public static PublicKey readPublic(File file) throws Exception {

        PKCS8EncodedKeySpec spec = readKey(file);
        KeyFactory kf = KeyFactory.getInstance(KEY_TYPE);
        return kf.generatePublic(spec);
    }

    public static PrivateKey readPrivate(File file) throws Exception {

        PKCS8EncodedKeySpec spec = readKey(file);
        KeyFactory kf = KeyFactory.getInstance(KEY_TYPE);
        return kf.generatePrivate(spec);
    }

    private static PKCS8EncodedKeySpec readKey(File file) throws IOException {
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        byte[] keyBytes = new byte[(int) file.length()];
        dis.readFully(keyBytes);
        dis.close();
        return new PKCS8EncodedKeySpec(keyBytes);
    }

}
