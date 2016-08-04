import com.box.sdk.BoxAPIConnection
import com.box.sdk.BoxDeveloperEditionAPIConnection
import com.box.sdk.BoxFolder
import com.box.sdk.BoxUser
import org.abelsromero.box.sdk.auth.EnterpriseAPIConnection
import org.abelsromero.box.sdk.helpers.RSAKeyReader

import java.nio.file.Files
import java.security.PrivateKey

import static org.abelsromero.box.sdk.helpers.FileHelpers.getFile

/**
 * Created by ABEL.SALGADOROMERO on 25/05/2016.
 */
this.addShutdownHook { println "END" }

def config = new ConfigSlurper().parse(Files.getResource('/config.groovy'))

/*
System.setProperty("http.proxyHost", config.proxy.host)
System.setProperty("http.proxyPort", config.proxy.port)
System.setProperty("https.proxyHost", config.proxy.host)
System.setProperty("https.proxyPort", config.proxy.port)
*/

// Params
def key = 'tia'

def clientId = config."$key".clientId
def keyId = config."$key".keyId
def enterpriseId = config."$key".enterpriseId
def keyLocation = config."$key".keyLocation

File privateKey = getFile(keyLocation)
PrivateKey signaturekey = RSAKeyReader.readPrivate(privateKey)

// Init API
EnterpriseAPIConnection enterpriseAPI = new EnterpriseAPIConnection(clientId, keyId, signaturekey)
enterpriseAPI.debug = true

// First authentication step
String token

/**
 * Use this to generate a token for admin purposes (e.g. create app users)
 */
// token = enterpriseAPI.generateEnterpriseTokenRequest(enterpriseId)

/**
 * Use this to generate a standard user token for an app user
 */
token = enterpriseAPI.generateAppUserRequest(config."$key".appUserId)

println token
println "=" * 36

// Second authenticattion step
String authToken = enterpriseAPI.authenticate(config."$key".secret, token)

/**
 * Do some real stuff
 */
BoxAPIConnection api = new BoxAPIConnection(authToken)

BoxUser.Info userInfo = BoxUser.getCurrentUser(api).getInfo();
println userInfo
