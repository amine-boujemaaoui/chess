package dev.boujemaaoui.chess.gameservice.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.boujemaaoui.chess.gameservice.player.Player;
import dev.boujemaaoui.chess.gameservice.player.PlayerRepository;
import dev.boujemaaoui.chess.gameservice.player.Role;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private static final TokenResponse TOKEN = new TokenResponse("token", "Bearer", 3600);

    private final PlayerRepository playerRepository = mock(PlayerRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final TokenService tokenService = mock(TokenService.class);
    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode("dummy-password")).thenReturn("dummy-hash");
        authService = new AuthService(playerRepository, passwordEncoder, tokenService);
    }

    @Test
    void registerStoresHashedPasswordAndReturnsToken() {
        when(passwordEncoder.encode("password123")).thenReturn("hash");
        when(playerRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenService.issue(any())).thenReturn(TOKEN);

        TokenResponse response = authService.register(new RegisterRequest("alice", "password123"));

        ArgumentCaptor<Player> saved = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo("alice");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("hash");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.PLAYER);
        assertThat(response).isEqualTo(TOKEN);
    }

    @Test
    void registerRejectsTakenUsername() {
        when(playerRepository.existsByUsernameIgnoreCase("Alice")).thenReturn(true);

        assertThatExceptionOfType(UsernameAlreadyTakenException.class)
                .isThrownBy(() -> authService.register(new RegisterRequest("Alice", "password123")));
        verify(playerRepository, never()).saveAndFlush(any());
    }

    @Test
    void registerRejectsUsernameTakenConcurrently() {
        when(playerRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatExceptionOfType(UsernameAlreadyTakenException.class)
                .isThrownBy(() -> authService.register(new RegisterRequest("alice", "password123")));
    }

    @Test
    void loginReturnsTokenWhenPasswordMatches() {
        Player alice = new Player("alice", "hash", Role.PLAYER, Instant.now());
        when(playerRepository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(alice));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);
        when(tokenService.issue(alice)).thenReturn(TOKEN);

        assertThat(authService.login(new LoginRequest("alice", "password123"))).isEqualTo(TOKEN);
    }

    @Test
    void loginRejectsWrongPassword() {
        Player alice = new Player("alice", "hash", Role.PLAYER, Instant.now());
        when(playerRepository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(alice));

        assertThatExceptionOfType(InvalidCredentialsException.class)
                .isThrownBy(() -> authService.login(new LoginRequest("alice", "wrong")));
        verify(tokenService, never()).issue(any());
    }

    @Test
    void loginWithUnknownUsernameStillChecksAPassword() {
        when(playerRepository.findByUsernameIgnoreCase("ghost")).thenReturn(Optional.empty());

        assertThatExceptionOfType(InvalidCredentialsException.class)
                .isThrownBy(() -> authService.login(new LoginRequest("ghost", "password123")));
        verify(passwordEncoder).matches(eq("password123"), anyString());
    }
}
