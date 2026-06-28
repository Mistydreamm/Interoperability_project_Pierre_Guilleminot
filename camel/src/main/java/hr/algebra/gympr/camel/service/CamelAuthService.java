package hr.algebra.gympr.camel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.gympr.camel.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class CamelAuthService {

    private String token;
    private String refreshToken;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gympr.admin.username}")
    private String adminUsername;
    @Value("${gympr.admin.password}")
    private String adminPassword;

    @Value("${gympr.api.login-url}")
    private String loginUrl;
    @Value("${gympr.api.refresh-url}")
    private String refreshUrl;

    public synchronized String getToken() {
        if (token == null) {
            authenticate();
        }
        return token;
    }

    public synchronized void tryTokenRefresh() {
        if (refreshToken == null || refreshToken.isEmpty()) {
            authenticate();
            return;
        }
        try {
            String jsonPayload = "{\"refreshToken\":\"" + refreshToken + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(refreshUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                TokenResponse tokenResponse = mapper.readValue(response.body(), TokenResponse.class);
                token = tokenResponse.getAccessToken();
                refreshToken = tokenResponse.getRefreshToken();
                System.out.println("Camel token refreshed successfully!");
            } else {
                System.err.println("Camel token refresh failed: Status " + response.statusCode() + ", Body: " + response.body() + ". Falling back to login...");
                authenticate();
            }
        } catch (Exception e) {
            System.err.println("Camel token refresh failed with exception: " + e.getMessage() );
            authenticate();
        }
    }

    private void authenticate() {
        try {
            String jsonPayload = "{\"username\":\"" + adminUsername + "\",\"password\":\"" + adminPassword + "\"}";
            System.out.println("CamelAuthService authenticating against URL: " + loginUrl + " with payload: " + jsonPayload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(loginUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                TokenResponse tokenResponse = mapper.readValue(response.body(), TokenResponse.class);
                token = tokenResponse.getAccessToken();
                refreshToken = tokenResponse.getRefreshToken();
            } else {
                throw new RuntimeException("Auth failed: Status " + response.statusCode() + ", Body: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not authenticate with gympr backend", e);
        }
    }
}
