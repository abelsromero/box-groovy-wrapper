import com.box.sdk.BoxAPIConnection
import com.box.sdk.BoxFolder
import com.box.sdk.BoxItem
import com.box.sdk.BoxUser

/**
 * Created by ABEL.SALGADOROMERO on 26/02/2016.
 */

def config = new ConfigSlurper().parse(new File('../../../src/main/resources/config.groovy').toURI().toURL())

System.setProperty("http.proxyHost", config.proxy.host)
System.setProperty("http.proxyPort", config.proxy.port)
System.setProperty("https.proxyHost", config.proxy.host)
System.setProperty("https.proxyPort", config.proxy.port)

// Easy way tp obtain token: https://box-token-generator.herokuapp.com/

BoxAPIConnection api = new BoxAPIConnection(config.box.token);

println "=== Root elements"
BoxFolder rootFolder = BoxFolder.getRootFolder(api)
for (BoxItem.Info item : rootFolder) {
    if (item.sharedLink) {
        println item.sharedLink.URL
    }
    // Prints (F) for folders and (D) for documents
    println "${(item instanceof BoxFolder.Info) ? '(F)' : '(D)'} $item.ID $item.name"
}

println "=== User info"
BoxUser.Info userInfo = BoxUser.getCurrentUser(api).getInfo()
System.out.format("Welcome, %s <%s>!\n\n", userInfo.getName(), userInfo.getLogin())

println "DONE!"

