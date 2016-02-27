import com.box.sdk.BoxAPIConnection
import com.box.sdk.BoxFolder
import com.box.sdk.BoxItem

/**
 * Created by ABEL.SALGADOROMERO on 26/02/2016.
 */

def config = new ConfigSlurper().parse(new File('../../../src/main/resources/config.groovy').toURI().toURL())

println config.proxy.host
println config.box
println config.box.token

System.setProperty("http.proxyHost", config.proxy.host)
System.setProperty("http.proxyPort", config.proxy.port)
System.setProperty("https.proxyHost", config.proxy.host)
System.setProperty("https.proxyPort", config.proxy.port)



// Obtain token from
// https://box-token-generator.herokuapp.com/

BoxAPIConnection api = new BoxAPIConnection(config.box.token);
BoxFolder rootFolder = BoxFolder.getRootFolder(api);
for (BoxItem.Info item : rootFolder) {
    println "$item.ID $item.name"
    println item.class
}

println "="*10

rootFolder.children.each {
    println it.class
}

rootFolder.getChildren()

println "DONE!"
