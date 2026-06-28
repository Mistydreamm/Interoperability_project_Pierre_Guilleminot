package hr.algebra.gympr.controller.rest;

import hr.algebra.gympr.dto.Dto;
import hr.algebra.gympr.entity.RefreshToken;
import hr.algebra.gympr.entity.User;
import hr.algebra.gympr.security.JwtService;
import hr.algebra.gympr.service.AuthService;
import hr.algebra.gympr.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login, register and token management")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthService authService;

    public AuthRestController(
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        RefreshTokenService refreshTokenService,
        AuthService authService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive JWT tokens")
    public ResponseEntity<Dto.TokenResponse> login(@Valid @RequestBody Dto.LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = (User) auth.getPrincipal();
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return ResponseEntity.ok(Dto.TokenResponse.of(accessToken, refreshToken.getToken(), 900_000L));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<String> register(@Valid @RequestBody Dto.RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("Registration successful.");
    }

    @PostMapping("/refresh")
    @Operation(summary = "Obtain a new access token using a refresh token")
    public ResponseEntity<?> refresh(@Valid @RequestBody Dto.RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.refreshToken())
            .filter(refreshTokenService::isValid)
            .map(rt -> {
                UserDetails user = rt.getUser();
                String newAccess = jwtService.generateAccessToken(user);
                RefreshToken newRefresh = refreshTokenService.createRefreshToken(rt.getUser());
                return ResponseEntity.ok(Dto.TokenResponse.of(newAccess, newRefresh.getToken(), 900_000L));
            })
            .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke the refresh token")
    public ResponseEntity<String> logout(@Valid @RequestBody Dto.RefreshTokenRequest request) {
        refreshTokenService.findByToken(request.refreshToken())
            .ifPresent(rt -> refreshTokenService.revokeByUser(rt.getUser()));
        return ResponseEntity.ok("Logged out.");
    }
}
