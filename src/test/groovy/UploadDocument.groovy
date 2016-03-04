import com.box.sdk.BoxAPIConnection
import com.box.sdk.BoxFile
import com.box.sdk.BoxFolder

import com.box.sdk.FileUploadParams
import com.box.sdk.ProgressListener

/**
 * Created by ABEL.SALGADOROMERO on 26/02/2016.
 */

def config = new ConfigSlurper().parse(new File('../../../src/main/resources/config.groovy').toURI().toURL())

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
BoxFolder folder = new BoxFolder(api, '6831337682')
println folder.info.name

// def is = getResource('Test Document 1.docx')
// def is = getResource('sample_10.txt')
// def is = getResource('sample_50.txt')
def is = getResource('sample_700.txt')

// Closest thing to a DSL ;)
BoxFile.Info file = folder.uploadFile([
        content : is,
        name    : "My_test_file-${System.currentTimeMillis()}.docx",
        created : new Date() - 10,
        listener: { numBytes, totalBytes ->
            println "[Uploading] NumKBytes: ${numBytes / 1024} KB - TotalBytes: $totalBytes"
        } as ProgressListener
] as FileUploadParams)

println "$file.ID $file.name"
println "$file.createdAt - $file.contentCreatedAt"

// Not working yet
// Metadata metadata = new Metadata()
// metadata.add("/my_description","some text")
//metadata.add("my_description","some text2")

println "DONE!"

def getResource(String name) {
    this.class.classLoader.getResourceAsStream("docs/$name")
}