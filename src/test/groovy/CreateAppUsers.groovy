import com.box.sdk.BoxAPIConnection
import com.box.sdk.BoxDeveloperEditionAPIConnection
import com.box.sdk.BoxUser
import com.box.sdk.CreateUserParams
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

// First authenticattion step
token = enterpriseAPI.generateEnterpriseTokenRequest(enterpriseId)
token = enterpriseAPI.generateAppUserRequest()

// Second authenticattion step
String authToken = enterpriseAPI.authenticate(config."$key".secret, token)
println "Token:: $authToken"

/**
 * Create users
 */
BoxAPIConnection api = new BoxAPIConnection(authToken)

CreateUserParams params = new CreateUserParams()
params.spaceAmount = 512 * 1024 * 1024 //512 MB

/*
def usersCount = 3
def usersBaseName = ''

(1..usersCount).each {
    def name = "$usersBaseName$it"
    println "Creating user: $name"
    BoxUser.Info user = BoxUser.createAppUser(api, name, params)

    println "User created with name $it and id ${user.ID} and login: ${user.login}"
}
*/


