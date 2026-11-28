package dev.boujemaaoui.chess.gameservice.auth;

import dev.boujemaaoui.chess.gameservice.player.Player;
import dev.boujemaaoui.chess.gameservice.player.PlayerRepository;
import dev.boujemaaoui.chess.gameservice.player.Role;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final String dummyPasswordHash;

    public AuthService(PlayerRepository playerRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password");
    }

    /** Crée un compte joueur et renvoie directement un jeton, pour éviter une connexion juste après. */
    public TokenResponse register(RegisterRequest request) {
        if (playerRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new UsernameAlreadyTakenException(request.username());
        }
        Player player = new Player(request.username(), passwordEncoder.encode(request.password()), Role.PLAYER, Instant.now());
        try {
            player = playerRepository.saveAndFlush(player);
        } catch (DataIntegrityViolationException e) {
            // Deux inscriptions simultanées : l'index unique de la base départage.
            throw new UsernameAlreadyTakenException(request.username());
        }
        return tokenService.issue(player);
    }

    public TokenResponse login(LoginRequest request) {
        Player player = playerRepository.findByUsernameIgnoreCase(request.username()).orElse(null);
        if (player == null) {
            // Même coût de hachage qu'avec un compte existant : la durée de réponse ne révèle pas les comptes.
            passwordEncoder.matches(request.password(), dummyPasswordHash);
            throw new InvalidCredentialsException();
        }
        if (!passwordEncoder.matches(request.password(), player.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return tokenService.issue(player);
    }
}
