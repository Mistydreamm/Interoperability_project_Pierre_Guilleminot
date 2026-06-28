package hr.algebra.project.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.project.frontend.model.LoginRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class AuthService {

    private static final String LOGIN_URL = "http://localhost:8081/api/auth/login";
    private static final String LOGOUT_URL = "http://localhost:8081/api/auth/logout";
    private static final String REFRESH_URL = "http://localhost:8081/api/auth/refresh";
    private static final String REGISTER_URL = "http://localhost:8081/api/auth/register";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void register(String username, String email, String password) throws Exception {
        var payload = java.util.Map.of(
            "username", username,
            "email", email,
            "password", password
        );
        String requestBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(REGISTER_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to register (Status: " + response.statusCode() + "): " + response.body());
        }
    }

    public String login(LoginRequest loginRequest) throws Exception {
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LOGIN_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("Failed to login (Status: " + response.statusCode() + "): " + response.body());
        }
    }

    public void logout(String refreshToken) throws Exception {
        var payload = java.util.Map.of("refreshToken", refreshToken);
        String requestBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LOGOUT_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to logout (Status: " + response.statusCode() + "): " + response.body());
        }
    }

    public String refresh(String refreshToken) throws Exception {
        Map<String, String> payload = java.util.Map.of("refreshToken", refreshToken);
        String requestBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(REFRESH_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("Failed to refresh token (Status: " + response.statusCode() + "): " + response.body());
        }
    }
}
