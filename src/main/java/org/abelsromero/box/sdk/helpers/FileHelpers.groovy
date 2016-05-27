package org.abelsromero.box.sdk.helpers

/**
 * Created by ABEL.SALGADOROMERO on 27/05/2016.
 */
class FileHelpers {

    static InputStream getResource(String name) {
        FileHelpers.class.classLoader.getResourceAsStream(name)
    }

    static File getFile(String name) {
        new File(FileHelpers.class.classLoader.getResource(name).toURI())
    }

}
