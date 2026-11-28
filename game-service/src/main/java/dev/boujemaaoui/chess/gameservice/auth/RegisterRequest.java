package dev.boujemaaoui.chess.gameservice.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;

public record RegisterRequest(
        @NotNull
        @Pattern(regexp = "[A-Za-z0-9_-]{3,20}",
                message = "doit contenir de 3 à 20 lettres non accentuées, chiffres, tirets ou tirets bas")
        String username,

        @NotNull
        @Size(min = 8, message = "doit contenir au moins 8 caractères")
        String password) {

    /** BCrypt ignore tout ce qui dépasse 72 octets : un mot de passe plus long serait tronqué en silence. */
    @AssertTrue(message = "ne doit pas dépasser 72 octets")
    boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
