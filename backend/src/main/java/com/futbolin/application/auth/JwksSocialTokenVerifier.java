package com.futbolin.application.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futbolin.core.exception.ApiException;
import com.futbolin.core.exception.ErrorCode;
import com.futbolin.core.props.AppProperties;
import com.futbolin.core.util.Codes;
import com.futbolin.domain.user.AuthProvider;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwksSocialTokenVerifier implements SocialTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(JwksSocialTokenVerifier.class);
    private static final String GOOGLE_JWKS = "https://www.googleapis.com/oauth2/v3/certs";
    private static final String APPLE_JWKS = "https://appleid.apple.com/auth/keys";

    private final AppProperties properties;
    private final ObjectMapper objectMapper;
    private final Map<AuthProvider, ConfigurableJWTProcessor<SecurityContext>> processors = new ConcurrentHashMap<>();

    public JwksSocialTokenVerifier(AppProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public SocialIdentity verify(AuthProvider provider, String idToken) {
        boolean insecure = properties.oauth() != null && properties.oauth().allowInsecureDev();
        boolean audienceConfigured = audience(provider) != null && !audience(provider).isBlank();
        if (!insecure || audienceConfigured) {
            try {
                return verifySignature(provider, idToken);
            } catch (ApiException e) {
                throw e;
            } catch (Exception e) {
                log.warn("JWKS verification failed for {}: {}", provider, e.getMessage());
                if (!insecure) {
                    throw new ApiException(ErrorCode.INVALID_TOKEN);
                }
            }
        }
        if (!insecure) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
        return decodeUnsigned(idToken, provider);
    }

    private SocialIdentity verifySignature(AuthProvider provider, String idToken) throws Exception {
        ConfigurableJWTProcessor<SecurityContext> processor = processors.computeIfAbsent(provider, this::processor);
        JWTClaimsSet claims = processor.process(idToken, null);
        Date exp = claims.getExpirationTime();
        if (exp == null || exp.toInstant().isBefore(Instant.now().minusSeconds(30))) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
        String iss = claims.getIssuer();
        if (provider == AuthProvider.GOOGLE && iss != null && !iss.contains("accounts.google.com")) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
        if (provider == AuthProvider.APPLE && iss != null && !iss.contains("appleid.apple.com")) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
        String expectedAud = audience(provider);
        if (expectedAud != null && !expectedAud.isBlank()) {
            if (claims.getAudience() == null || claims.getAudience().stream().noneMatch(a -> a.contains(expectedAud))) {
                throw new ApiException(ErrorCode.INVALID_TOKEN);
            }
        }
        String email = claims.getStringClaim("email");
        String sub = claims.getSubject();
        if (email == null || sub == null) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
        return new SocialIdentity(sub, Codes.normalizeEmail(email));
    }

    private ConfigurableJWTProcessor<SecurityContext> processor(AuthProvider provider) {
        try {
            String url = provider == AuthProvider.APPLE ? APPLE_JWKS : GOOGLE_JWKS;
            var source = JWKSourceBuilder.create(URI.create(url).toURL()).retrying(true).build();
            ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, source));
            return processor;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot init JWKS for " + provider, e);
        }
    }

    private SocialIdentity decodeUnsigned(String idToken, AuthProvider provider) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                throw new ApiException(ErrorCode.INVALID_TOKEN);
            }
            JsonNode json = objectMapper.readTree(new String(Base64.getUrlDecoder().decode(parts[1])));
            String email = json.path("email").asText(null);
            String sub = json.path("sub").asText(null);
            if (email == null || sub == null) {
                throw new ApiException(ErrorCode.INVALID_TOKEN);
            }
            String audience = json.path("aud").asText("");
            String expected = audience(provider);
            if (expected != null && !expected.isBlank() && !audience.contains(expected)) {
                throw new ApiException(ErrorCode.INVALID_TOKEN);
            }
            return new SocialIdentity(sub, Codes.normalizeEmail(email));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
    }

    private String audience(AuthProvider provider) {
        if (properties.oauth() == null) {
            return null;
        }
        return provider == AuthProvider.APPLE ? properties.oauth().appleAudience() : properties.oauth().googleClientId();
    }
}
