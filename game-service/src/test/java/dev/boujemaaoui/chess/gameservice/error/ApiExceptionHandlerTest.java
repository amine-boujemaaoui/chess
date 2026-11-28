package dev.boujemaaoui.chess.gameservice.error;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Filtres de sécurité désactivés : ce test ne porte que sur la traduction des erreurs.
@WebMvcTest(ApiExceptionHandlerTest.FailingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, ApiExceptionHandlerTest.FailingController.class})
class ApiExceptionHandlerTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void unknownRouteReturnsNotFound() {
        assertThat(mvc.get().uri("/unknown"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath("$.status").isEqualTo(404);
    }

    @Test
    void businessExceptionKeepsItsStatus() {
        assertThat(mvc.get().uri("/test/conflict"))
                .hasStatus(HttpStatus.CONFLICT)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath("$.status").isEqualTo(409);
    }

    @Test
    void invalidRequestBodyReturnsBadRequest() {
        assertThat(mvc.post().uri("/test/validated").contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"\"}"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void unexpectedErrorHidesItsMessage() {
        var response = mvc.get().uri("/test/crash").exchange();

        assertThat(response)
                .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath("$.detail").isEqualTo("Une erreur inattendue est survenue.");
        assertThat(response).bodyText().doesNotContain("secret interne");
    }

    @RestController
    static class FailingController {

        record NamedRequest(@NotBlank String name) {
        }

        @GetMapping("/test/conflict")
        void conflict() {
            throw new ErrorResponseException(HttpStatus.CONFLICT);
        }

        @PostMapping("/test/validated")
        void validated(@Valid @RequestBody NamedRequest request) {
        }

        @GetMapping("/test/crash")
        void crash() {
            throw new IllegalStateException("secret interne");
        }
    }
}
