package hr.algebra.project.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hr.algebra.project.frontend.controller.LoginRequestController;
import hr.algebra.project.frontend.model.PersonalRecord;
import hr.algebra.project.frontend.model.TokenResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PersonalRecordService {

    private static final String RECORDS_URL = "http://localhost:8081/api/lifts";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final AuthService authService = new AuthService();

    private HttpResponse<String> executeRequest(HttpRequest.Builder builder) throws Exception {
        if (LoginRequestController.authToken == null || LoginRequestController.authToken.isEmpty()) {
            throw new IllegalStateException("Auth token is not available.");
        }

        HttpRequest request = builder.header("Authorization", "Bearer " + LoginRequestController.authToken).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            System.out.println("Access token expired (401). Attempting token refresh...");
            if (tryTokenRefresh()) {
                request = builder.header("Authorization", "Bearer " + LoginRequestController.authToken).build();
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            }
        }
        return response;
    }

    private boolean tryTokenRefresh() {
        if (LoginRequestController.refreshToken == null || LoginRequestController.refreshToken.isEmpty()) {
            return false;
        }
        try {
            String responseBody = authService.refresh(LoginRequestController.refreshToken);
            TokenResponse tokenResponse = mapper.readValue(responseBody, TokenResponse.class);
            LoginRequestController.authToken = tokenResponse.getAccessToken();
            LoginRequestController.refreshToken = tokenResponse.getRefreshToken();
            System.out.println("Token refreshed successfully! New token: " + LoginRequestController.authToken);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to refresh token: " + e.getMessage());
            return false;
        }
    }

    public List<PersonalRecord> getAllRecords() throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(RECORDS_URL))
                .header("Content-Type", "application/json")
                .GET();

        HttpResponse<String> response = executeRequest(builder);

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), new TypeReference<List<PersonalRecord>>() {});
        } else {
            throw new RuntimeException("Failed to fetch records: " + response.body());
        }
    }

    public PersonalRecord getOneRecord(int id) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(RECORDS_URL + "/" + id))
                .header("Content-Type", "application/json")
                .GET();

        HttpResponse<String> response = executeRequest(builder);

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), PersonalRecord.class);
        } else {
            throw new RuntimeException("Failed to fetch record details: " + response.body());
        }
    }

    public void postRecord(PersonalRecord record) throws Exception {
        String json = mapper.writeValueAsString(record);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(RECORDS_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));

        HttpResponse<String> response = executeRequest(builder);

        if (response.statusCode() == 201) {
            System.out.println("Record posted successfully");
        } else {
            throw new RuntimeException("Failed Post records: " + response.body());
        }
    }

    public void putRecord(PersonalRecord record, int id) throws Exception {
        String json = mapper.writeValueAsString(record);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(RECORDS_URL + "/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json));

        HttpResponse<String> response = executeRequest(builder);

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            System.out.println("Record updated successfully");
        } else {
            throw new RuntimeException("Failed to update record: " + response.body());
        }
    }

    public void deleteRecord(int id) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(RECORDS_URL + "/" + id))
                .DELETE();

        HttpResponse<String> response = executeRequest(builder);

        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new RuntimeException("Failed to delete record: " + response.body());
        }
    }

    public void backupDatabase() throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/database/backup"))
                .POST(HttpRequest.BodyPublishers.noBody());

        HttpResponse<String> response = executeRequest(builder);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to backup database: " + response.body());
        }
    }

    public void restoreDatabase() throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/database/restore"))
                .POST(HttpRequest.BodyPublishers.noBody());

        HttpResponse<String> response = executeRequest(builder);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to restore database: " + response.body());
        }
    }
}
