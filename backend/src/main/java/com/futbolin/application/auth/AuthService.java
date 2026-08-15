package com.futbolin.application.auth;

import com.futbolin.api.dto.LoginRequest;
import com.futbolin.api.dto.RegisterRequest;
import com.futbolin.api.dto.SocialLoginRequest;
import com.futbolin.api.dto.TokenResponse;
import com.futbolin.core.exception.ApiException;
import com.futbolin.core.exception.ErrorCode;
import com.futbolin.core.props.AppProperties;
import com.futbolin.core.security.JwtService;
import com.futbolin.core.util.Codes;
import com.futbolin.core.util.Hashes;
import com.futbolin.data.entity.PasswordResetTokenEntity;
import com.futbolin.data.entity.RefreshTokenEntity;
import com.futbolin.data.entity.UserEntity;
import com.futbolin.data.entity.UserProfileEntity;
import com.futbolin.data.repository.PasswordResetTokenRepository;
import com.futbolin.data.repository.RefreshTokenRepository;
import com.futbolin.data.repository.UserRepository;
import com.futbolin.domain.ranking.Division;
import com.futbolin.domain.user.AuthProvider;
import com.futbolin.domain.user.Role;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties properties;
    private final SocialTokenVerifier socialTokenVerifier;

    public AuthService(
            UserRepository users,
            RefreshTokenRepository refreshTokens,
            PasswordResetTokenRepository resetTokens,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AppProperties properties,
            SocialTokenVerifier socialTokenVerifier
    ) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.resetTokens = resetTokens;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.properties = properties;
        this.socialTokenVerifier = socialTokenVerifier;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String email = Codes.normalizeEmail(request.email());
        String username = Codes.normalizeUsername(request.username());
        if (users.existsByEmail(email)) {
            throw new ApiException(ErrorCode.EMAIL_TAKEN);
        }
        if (users.existsByUsername(username)) {
            throw new ApiException(ErrorCode.USERNAME_TAKEN);
        }
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setProvider(AuthProvider.LOCAL);
        user.setRole(Role.USER);
        user.setEnabled(true);
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUser(user);
        profile.setDisplayName(request.displayName() == null || request.displayName().isBlank()
                ? request.username() : request.displayName());
        profile.setCountry(request.country());
        profile.setFavoriteTeam(request.favoriteTeam());
        profile.setDivision(Division.AMATEUR);
        profile.setRankingPoints(1000);
        profile.setPeakRankingPoints(1000);
        user.setProfile(profile);
        users.save(user);
        return issue(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        String login = request.login().trim().toLowerCase();
        UserEntity user = users.findByEmail(login)
                .or(() -> users.findByUsername(login))
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS));
        if (user.isLocked() || !user.isEnabled()) {
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }
        user.setLastLoginAt(Instant.now());
        return issue(user);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parse(refreshToken);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
        if (!"refresh".equals(claims.get("typ", String.class))) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
        RefreshTokenEntity stored = refreshTokens.findByTokenHash(Hashes.sha256(refreshToken))
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
        stored.setRevoked(true);
        UserEntity user = stored.getUser();
        return issue(user);
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokens.deleteByUserId(userId);
    }

    @Transactional
    public void forgotPassword(String email) {
        users.findByEmail(Codes.normalizeEmail(email)).ifPresent(user -> {
            PasswordResetTokenEntity token = new PasswordResetTokenEntity();
            String raw = UUID.randomUUID().toString();
            token.setUser(user);
            token.setTokenHash(Hashes.sha256(raw));
            token.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
            resetTokens.save(token);
            log.info("Password reset token for {} is {}", user.getEmail(), raw);
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetTokenEntity stored = resetTokens.findByTokenHashAndUsedFalse(Hashes.sha256(token))
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
        stored.setUsed(true);
        stored.getUser().setPasswordHash(passwordEncoder.encode(newPassword));
        refreshTokens.deleteByUserId(stored.getUser().getId());
    }

    @Transactional
    public TokenResponse socialLogin(SocialLoginRequest request) {
        AuthProvider provider;
        try {
            provider = AuthProvider.valueOf(request.provider().toUpperCase());
        } catch (Exception e) {
            throw new ApiException(ErrorCode.VALIDATION, "Unsupported provider");
        }
        if (provider == AuthProvider.LOCAL) {
            throw new ApiException(ErrorCode.VALIDATION, "Unsupported provider");
        }
        SocialIdentity identity = socialTokenVerifier.verify(provider, request.idToken());
        UserEntity user = users.findByProviderAndProviderId(provider, identity.subject())
                .or(() -> users.findByEmail(identity.email()))
                .orElseGet(() -> createSocialUser(provider, identity, request.username()));
        if (user.getProvider() == AuthProvider.LOCAL) {
            user.setProvider(provider);
            user.setProviderId(identity.subject());
        }
        user.setLastLoginAt(Instant.now());
        return issue(user);
    }

    private UserEntity createSocialUser(AuthProvider provider, SocialIdentity identity, String preferredUsername) {
        String username = Codes.normalizeUsername(
                preferredUsername != null ? preferredUsername : identity.email().split("@")[0]
        );
        String base = username;
        int i = 1;
        while (users.existsByUsername(username)) {
            username = base + i++;
        }
        UserEntity user = new UserEntity();
        user.setEmail(identity.email());
        user.setUsername(username);
        user.setProvider(provider);
        user.setProviderId(identity.subject());
        user.setEmailVerified(true);
        user.setRole(Role.USER);
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUser(user);
        profile.setDisplayName(username);
        user.setProfile(profile);
        return users.save(user);
    }

    private TokenResponse issue(UserEntity user) {
        String access = jwtService.createAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refresh = jwtService.createRefreshToken(user.getId());
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUser(user);
        entity.setTokenHash(Hashes.sha256(refresh));
        entity.setExpiresAt(Instant.now().plus(properties.jwt().refreshTokenDays(), ChronoUnit.DAYS));
        refreshTokens.save(entity);
        return TokenResponse.of(access, refresh, properties.jwt().accessTokenMinutes() * 60, user.getId(), user.getUsername());
    }
}
