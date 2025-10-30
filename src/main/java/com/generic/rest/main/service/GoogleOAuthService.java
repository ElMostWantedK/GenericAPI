package com.generic.rest.main.service;

import com.generic.rest.main.security.GoogleOAuthProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Service for validating Google OAuth tokens
 */
@Service
public class GoogleOAuthService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleOAuthService(GoogleOAuthProperties oAuthProperties) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(oAuthProperties.getClientId()))
                .build();
    }

    /**
     * Validates a Google OAuth token and returns user information
     *
     * @param tokenString The Google ID token to validate
     * @return GoogleUserInfo containing user data from the token
     * @throws RuntimeException if token is invalid or verification fails
     */
    public GoogleUserInfo validateToken(String tokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(tokenString);

            if (idToken == null) {
                throw new RuntimeException("Invalid Google OAuth token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            // Extract user information from token payload
            String email = payload.getEmail();
            boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");
            String givenName = (String) payload.get("given_name");
            String familyName = (String) payload.get("family_name");
            String pictureUrl = (String) payload.get("picture");

            if (!emailVerified) {
                throw new RuntimeException("Email not verified by Google");
            }

            return new GoogleUserInfo(
                    email,
                    name,
                    givenName != null ? givenName : "",
                    familyName != null ? familyName : "",
                    pictureUrl
            );

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to validate Google OAuth token: " + e.getMessage(), e);
        }
    }

    /**
     * Record to hold Google user information extracted from the token
     */
    public record GoogleUserInfo(
            String email,
            String name,
            String givenName,
            String familyName,
            String pictureUrl
    ) {
    }
}
