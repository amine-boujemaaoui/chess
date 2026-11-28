package dev.boujemaaoui.chess.gameservice.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import dev.boujemaaoui.chess.gameservice.TestcontainersConfiguration;
import dev.boujemaaoui.chess.gameservice.player.Player;
import dev.boujemaaoui.chess.gameservice.player.PlayerRepository;
import dev.boujemaaoui.chess.gameservice.player.Role;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TokenService tokenService;

    @BeforeEach
    void cleanDatabase() {
        playerRepository.deleteAll();
    }

    @Test
    void registeredPlayerCanAccessTheirProfile() {
        MvcTestResult registration = post("/api/auth/register", "alice", "password123");
        assertThat(registration).hasStatus(HttpStatus.CREATED);
        assertThat(registration).bodyJson().extractingPath("$.tokenType").isEqualTo("Bearer");

        assertThat(mvc.get().uri("/api/players/me").header(HttpHeaders.AUTHORIZATION, bearer(registration)))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.username", username -> assertThat(username).asString().isEqualTo("alice"))
                .hasPathSatisfying("$.role", role -> assertThat(role).asString().isEqualTo("PLAYER"))
                .doesNotHavePath("$.passwordHash");
    }

    @Test
    void storesOnlyAPasswordHash() {
        post("/api/auth/register", "alice", "password123");

        Player alice = playerRepository.findByUsernameIgnoreCase("alice").orElseThrow();
        assertThat(alice.getPasswordHash()).startsWith("$2").doesNotContain("password123");
    }

    @Test
    void rejectsUsernameAlreadyTakenWhateverTheCase() {
        post("/api/auth/register", "alice", "password123");

        assertThat(post("/api/auth/register", "ALICE", "password456"))
                .hasStatus(HttpStatus.CONFLICT)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void rejectsInvalidRegistration() {
        assertThat(post("/api/auth/register", "a!", "password123")).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(post("/api/auth/register", "alice", "short")).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(post("/api/auth/register", "alice", "é".repeat(37))).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        post("/api/auth/register", "alice", "password123");

        MvcTestResult login = post("/api/auth/login", "Alice", "password123");

        assertThat(login).hasStatusOk();
        assertThat(mvc.get().uri("/api/players/me").header(HttpHeaders.AUTHORIZATION, bearer(login))).hasStatusOk();
    }

    @Test
    void loginRejectsWrongPasswordAndUnknownUserTheSameWay() {
        post("/api/auth/register", "alice", "password123");

        MvcTestResult wrongPassword = post("/api/auth/login", "alice", "wrong-password");
        MvcTestResult unknownUser = post("/api/auth/login", "bob", "password123");

        for (MvcTestResult result : new MvcTestResult[] {wrongPassword, unknownUser}) {
            assertThat(result)
                    .hasStatus(HttpStatus.UNAUTHORIZED)
                    .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .bodyJson().extractingPath("$.detail").isEqualTo("Nom d'utilisateur ou mot de passe incorrect.");
        }
    }

    @Test
    void protectedRouteRequiresAValidToken() {
        assertThat(mvc.get().uri("/api/players/me"))
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .hasHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        assertThat(mvc.get().uri("/api/players/me").header(HttpHeaders.AUTHORIZATION, "Bearer forged.token.value"))
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void adminRoutesAreReservedToAdmins() {
        Player player = playerRepository.save(new Player("alice", "hash", Role.PLAYER, Instant.now()));
        Player admin = playerRepository.save(new Player("root", "hash", Role.ADMIN, Instant.now()));

        assertThat(mvc.get().uri("/api/admin/anything").header(HttpHeaders.AUTHORIZATION, bearer(player)))
                .hasStatus(HttpStatus.FORBIDDEN)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        // Le rôle ADMIN passe la sécurité ; aucune route d'administration n'existe encore, d'où le 404.
        assertThat(mvc.get().uri("/api/admin/anything").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    private MvcTestResult post(String uri, String username, String password) {
        String body = "{\"username\": \"%s\", \"password\": \"%s\"}".formatted(username, password);
        return mvc.post().uri(uri).contentType(MediaType.APPLICATION_JSON).content(body).exchange();
    }

    private static String bearer(MvcTestResult result) {
        try {
            return "Bearer " + JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private String bearer(Player player) {
        return "Bearer " + tokenService.issue(player).accessToken();
    }
}
