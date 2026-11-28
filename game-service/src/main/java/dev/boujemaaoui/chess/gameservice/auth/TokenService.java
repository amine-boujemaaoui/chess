package dev.boujemaaoui.chess.gameservice.auth;

import dev.boujemaaoui.chess.gameservice.player.Player;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Émet les jetons JWT : le sujet est l'identifiant du joueur, la revendication {@code roles} son rôle. */
@Service
public class TokenService {

    static final String ROLES_CLAIM = "roles";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public TokenService(JwtEncoder jwtEncoder, JwtProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public TokenResponse issue(Player player) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(player.getId()))
                .issuedAt(now)
                .expiresAt(now.plus(properties.expiration()))
                .claim(ROLES_CLAIM, List.of(player.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", properties.expiration().toSeconds());
    }
}
