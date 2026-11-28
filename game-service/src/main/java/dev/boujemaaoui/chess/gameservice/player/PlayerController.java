package dev.boujemaaoui.chess.gameservice.player;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
@SecurityRequirement(name = "bearer")
public class PlayerController {

    private final PlayerRepository playerRepository;

    public PlayerController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /** Joueur connecté, identifié par le sujet de son jeton. */
    @GetMapping("/me")
    public PlayerResponse me(@AuthenticationPrincipal Jwt jwt) {
        long id = Long.parseLong(jwt.getSubject());
        return playerRepository.findById(id)
                .map(PlayerResponse::from)
                .orElseThrow(() -> new PlayerNotFoundException(id));
    }
}
