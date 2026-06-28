package hr.algebra.gympr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class Dto {

    public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
    ) {}

    public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String password
    ) {}

    public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
    ) {
        public static TokenResponse of(String access, String refresh, long expiresIn) {
            return new TokenResponse(access, refresh, "Bearer", expiresIn);
        }
    }

    public record RefreshTokenRequest(
        @NotBlank String refreshToken
    ) {}
}
