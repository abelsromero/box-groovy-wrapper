package org.abelsromero.box.sdk.auth

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import org.apache.http.HttpResponse
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims

import java.security.PrivateKey

/**
 * Creates a JWT token to be used in an OAth2 request to create a proper org.abelsromero.box.auth token to creat app users
 *
 * Created by ABEL.SALGADOROMERO on 20/05/2016.
 */
public class EnterpriseAPIConnection {

    private String clientId
    private String keyId
    private PrivateKey signatureKey

    private boolean debug = false

    public EnterpriseAPIConnection(String clientId, String keyId, PrivateKey signatureKey) {
        this.clientId = clientId
        this.keyId = keyId
        this.signatureKey = signatureKey
    }

    // https://box-content.readme.io/docs/app-auth
    public String generateAppUserRequest(String userId) {
        return generateJwtPayload(clientId, userId, ['box_sub_type': 'user'])
    }

    public String generateEnterpriseTokenRequest(String enterpriseId) {
        return generateJwtPayload(clientId, enterpriseId, ['box_sub_type': 'enterprise'])
    }

    private String generateJwtPayload(String issuer, String subject, Map<String, String> properties) {

        JwtClaims claims = new JwtClaims()
        claims.setIssuer(issuer)  // iss
        claims.setSubject(subject)

        properties?.each { k, v ->
            claims.setStringClaim(k, v)
        }

        claims.setAudience('https://api.box.com/oauth2/token')
        claims.setGeneratedJwtId(); // random unique identifier for the token
        claims.setExpirationTimeMinutesInTheFuture(1) // exp

        JsonWebSignature jws = new JsonWebSignature()

        jws.setPayload(claims.toJson())
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256)
        jws.setKeyIdHeaderValue(keyId)

        if (debug) {
            println jws.toString()
            println JsonOutput.prettyPrint(claims.toJson())
        }

        // The JWT is signed using the private key
        jws.setKey(signatureKey)

        // Sign the JWS and produce the compact serialization or the complete JWT/JWS
        // representation, which is a string consisting of three dot ('.') separated
        // base64url-encoded parts in the form Header.Payload.Signature
        // If you wanted to encrypt it, you can simply set this jwt as the payload
        // of a JsonWebEncryption object and set the cty (Content Type) header to "jwt".
        return jws.getCompactSerialization()
    }


    String authenticate(String secret, String jwtPayload) {

        new HTTPBuilder('https://www.box.com/api').request(Method.POST) {
            uri.path = '/api/oauth2/token'
            send(ContentType.URLENC, [
                    'grant_type'   : 'urn:ietf:params:oauth:grant-type:jwt-bearer',
                    'client_id'    : clientId,
                    'client_secret': secret,
                    'assertion'    : jwtPayload
            ])
            def token
            response.success = { resp ->
                token = new JsonSlurper().parse(resp.entity.content).access_token
            }
            return token
        }
    }

}