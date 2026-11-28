package dev.boujemaaoui.chess.gameservice.auth;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotNull String username, @NotNull String password) {
}
