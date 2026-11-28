package dev.boujemaaoui.chess.gameservice.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param secret clé HMAC de signature des jetons ; HS256 exige au moins 256 bits
 * @param expiration durée de validité d'un jeton
 */
@Validated
@ConfigurationProperties("chess.jwt")
public record JwtProperties(
        @NotNull @Size(min = 32, message = "doit contenir au moins 32 caractères (variable JWT_SECRET)") String secret,
        @NotNull Duration expiration) {
}
