package dev.boujemaaoui.chess.gameservice.auth;

/**
 * Jeton d'accès à envoyer dans l'en-tête {@code Authorization: Bearer <jeton>}.
 *
 * @param expiresIn durée de validité, en secondes
 */
public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
