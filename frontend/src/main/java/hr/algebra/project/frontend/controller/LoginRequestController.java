package hr.algebra.project.frontend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.project.frontend.GymPrClientApplication;
import hr.algebra.project.frontend.model.LoginRequest;
import hr.algebra.project.frontend.model.TokenResponse;
import hr.algebra.project.frontend.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginRequestController {

    @FXML
    private Label statusLabel;
    @FXML
    private Button LoginButton;
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private TextField email;
    @FXML
    private Label toggleModeLabel;

    private final AuthService authService = new AuthService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public static String authToken;
    public static String refreshToken;
    private boolean isLoginMode = true;



    @FXML
    private void onToggleMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            email.setVisible(false);
            password.setLayoutY(87.0);
            LoginButton.setLayoutY(149.0);
            toggleModeLabel.setLayoutY(200.0);
            LoginButton.setText("LOGIN");
            toggleModeLabel.setText("Don't have an account? Register");
        } else {
            email.setVisible(true);
            password.setLayoutY(135.0);
            LoginButton.setLayoutY(197.0);
            toggleModeLabel.setLayoutY(248.0);
            LoginButton.setText("REGISTER");
            toggleModeLabel.setText("Already have an account? Login");
        }
        statusLabel.setText("");
    }

    @FXML
    private void onLoginAction() {
        if (username.getText().isEmpty() || password.getText().isEmpty() || (!isLoginMode && email.getText().isEmpty())) {
            statusLabel.setText("All fields must be filled!");
            return;
        }

        if (!isLoginMode) {
            if (username.getText().length() < 3) {
                statusLabel.setText("Username must be at least 3 characters!");
                return;
            }
            if (!email.getText().contains("@") || !email.getText().contains(".")) {
                statusLabel.setText("Please enter a valid email address!");
                return;
            }
            if (password.getText().length() < 6) {
                statusLabel.setText("Password must be at least 6 characters!");
                return;
            }
        }

        try {
            if (isLoginMode) {
                LoginRequest loginRequest = new LoginRequest(username.getText(), password.getText());
                String responseBody = authService.login(loginRequest);
                
                TokenResponse tokenResponse = objectMapper.readValue(responseBody, TokenResponse.class);
                authToken = tokenResponse.getAccessToken();
                refreshToken = tokenResponse.getRefreshToken();

                System.out.println("Login successful! Access Token: " + authToken + ", Refresh Token: " + refreshToken);
                statusLabel.setText("Login successful!");

                openMainView();
            } else {
                authService.register(username.getText(), email.getText(), password.getText());
                statusLabel.setText("Registration successful! Please login.");
                username.clear();
                email.clear();
                password.clear();
                onToggleMode(); // Revert back to login mode
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Status: 302")) {
                statusLabel.setText("Registration failed: Username or Email already exists!");
            } else {
                statusLabel.setText((isLoginMode ? "Login" : "Registration") + " failed: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
    
    private void openMainView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GymPrClientApplication.class.getResource("mainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        
        Stage stage = (Stage) LoginButton.getScene().getWindow();
        
        stage.setTitle("GymPR - Dashboard");
        stage.setScene(scene);
        stage.show();
    }
}
