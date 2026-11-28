package dev.boujemaaoui.chess.gameservice.player;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class PlayerNotFoundException extends ErrorResponseException {

    public PlayerNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Joueur introuvable : " + id), null);
    }
}
