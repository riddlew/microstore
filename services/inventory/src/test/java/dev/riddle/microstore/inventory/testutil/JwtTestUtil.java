package dev.riddle.microstore.inventory.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for generating test JWT tokens and JWKS for testing OAuth2 Resource Server.
 */
public class JwtTestUtil {

    private static final String TEST_ISSUER = "http://localhost:8888";
    private static KeyPair testKeyPair;
    private static RSAKey testRsaKey;

    static {
        try {
            testKeyPair = generateRsaKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) testKeyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) testKeyPair.getPrivate();
            testRsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID("test-key-id")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize test RSA key pair", e);
        }
    }

    /**
     * Generate a test JWT token with the specified scopes.
     */
    public static String generateTestJwt(String subject, List<String> scopes, String issuer) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issuer(issuer)
                    .audience(List.of(subject))
                    .claim("scope", String.join(" ", scopes))
                    .claim("roles", List.of("ROLE_USER"))
                    .claim("username", subject)
                    .issueTime(Date.from(now))
                    .notBeforeTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(3600)))
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(testRsaKey.getKeyID())
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(new RSASSASigner(testRsaKey.toPrivateKey()));

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate test JWT", e);
        }
    }

    /**
     * Generate a test JWT token with default issuer.
     */
    public static String generateTestJwt(String subject, List<String> scopes) {
        return generateTestJwt(subject, scopes, TEST_ISSUER);
    }

    /**
     * Get the JWKS JSON string for the test key pair.
     */
    public static String getJwksJson() {
        try {
            JWKSet jwkSet = new JWKSet(testRsaKey);
            Map<String, Object> jsonObject = jwkSet.toJSONObject();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonObject);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JWKS to JSON", e);
        }
    }

    /**
     * Get the test issuer URI.
     */
    public static String getTestIssuer() {
        return TEST_ISSUER;
    }

    private static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }
}

