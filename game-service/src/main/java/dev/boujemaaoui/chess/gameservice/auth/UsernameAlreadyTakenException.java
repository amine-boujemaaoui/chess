package dev.boujemaaoui.chess.gameservice.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class UsernameAlreadyTakenException extends ErrorResponseException {

    public UsernameAlreadyTakenException(String username) {
        super(HttpStatus.CONFLICT,
                ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Ce nom d'utilisateur est déjà pris : " + username),
                null);
    }
}
