package dev.boujemaaoui.chess.gameservice.player;

import java.time.Instant;

public record PlayerResponse(Long id, String username, Role role, Instant createdAt) {

    public static PlayerResponse from(Player player) {
        return new PlayerResponse(player.getId(), player.getUsername(), player.getRole(), player.getCreatedAt());
    }
}
