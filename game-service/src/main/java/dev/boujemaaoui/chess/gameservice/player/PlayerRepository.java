package dev.boujemaaoui.chess.gameservice.player;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);
}
