package dev.boujemaaoui.chess.gameservice.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/** Même message que le nom d'utilisateur existe ou non, pour ne pas révéler quels comptes existent. */
public class InvalidCredentialsException extends ErrorResponseException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED,
                ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Nom d'utilisateur ou mot de passe incorrect."),
                null);
    }
}
