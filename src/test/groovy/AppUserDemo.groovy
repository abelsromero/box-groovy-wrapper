import com.box.sdk.BoxAPIConnection
import com.box.sdk.BoxUser
import org.abelsromero.box.sdk.auth.EnterpriseAPIConnection
import org.abelsromero.box.sdk.helpers.RSAKeyReader

import java.security.PrivateKey

import static org.abelsromero.box.sdk.helpers.FileHelpers.getFile

/**
 * Created by ABEL.SALGADOROMERO on 25/05/2016.
 */
this.addShutdownHook { println "END" }

def config = new ConfigSlurper().parse(getFile('config.groovy').toURL())

/*
System.setProperty("http.proxyHost", config.proxy.host)
System.setProperty("http.proxyPort", config.proxy.port)
System.setProperty("https.proxyHost", config.proxy.host)
System.setProperty("https.proxyPort", config.proxy.port)
*/

// Params
def key = 'zecm'
// def key = 'randomapp'
// def key = 'sps'

def clientId = config."$key".clientId
def keyId = config."$key".keyId
def enterpriseId = config."$key".enterpriseId
def keyLocation = config."$key".keyLocation

File privateKey = getFile(keyLocation)
PrivateKey signaturekey = RSAKeyReader.readPrivate(privateKey)

// Init API
EnterpriseAPIConnection enterpriseAPI = new EnterpriseAPIConnection(clientId, keyId, signaturekey)
enterpriseAPI.debug = true

// First authenticattion step
String token
// token = enterpriseAPI.generateEnterpriseTokenRequest(enterpriseId)
println token
println "=" * 36
token = enterpriseAPI.generateAppUserRequest('user_id')

// Seconf authenticattion step
String authToken = enterpriseAPI.authenticate(config."$key".secret, token)

/**
 * Do real stuff
 */
BoxAPIConnection api = new BoxAPIConnection(authToken)
// do stuff...



