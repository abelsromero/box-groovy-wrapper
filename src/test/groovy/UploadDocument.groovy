import com.box.sdk.BoxAPIConnection
import com.box.sdk.BoxFile
import com.box.sdk.BoxFolder

import com.box.sdk.FileUploadParams
import com.box.sdk.ProgressListener

/**
 * Created by ABEL.SALGADOROMERO on 26/02/2016.
 */
def config = new ConfigSlurper().parse(getFile('config.groovy').toURL())

System.setProperty("http.proxyHost", config.proxy.host)
System.setProperty("http.proxyPort", config.proxy.port)
System.setProperty("https.proxyHost", config.proxy.host)
System.setProperty("https.proxyPort", config.proxy.port)

// Obtain token from
// https://box-token-generator.herokuapp.com/

BoxAPIConnection api = new BoxAPIConnection(config.box.token);

// Test connection
BoxFolder.getRootFolder()

println "=== Test ${this.class.name}"
// Test connection
BoxFolder rootFolder = BoxFolder.getRootFolder(api)

// Get target folder by Id
BoxFolder folder = new BoxFolder(api, config.box.folder)
println folder.info.name

def filename = 'docs/Test Document 1.docx'

// Closest thing to a DSL ;)
BoxFile.Info file = folder.uploadFile([
        content : getResource(filename),
        name    : "My_test_file-${System.currentTimeMillis()}.docx",
        created : new Date() - 10, /* ContentCreatedAt can be manipulated */
        size    : getFile(filename).size(),  /* Size must be set manually */
        listener: { numBytes, totalBytes ->
            println "[Uploading] NumKBytes: ${numBytes / 1024} KB - TotalBytes: $totalBytes"
        } as ProgressListener,
] as FileUploadParams)

println "ID: $file.ID, filenam: $file.name"
println "CreatedAt: $file.createdAt, ContentCreatedAt: $file.contentCreatedAt"

/**
 * Utils
 */
InputStream getResource(String name) {
    this.class.classLoader.getResourceAsStream(name)
}

File getFile(String name) {
    new File(this.class.classLoader.getResource(name).toURI())
}

addShutdownHook {
    println "DONE!"
}