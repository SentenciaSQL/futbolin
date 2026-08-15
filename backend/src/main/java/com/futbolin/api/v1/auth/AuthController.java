package com.futbolin.api.v1.auth;

import com.futbolin.api.dto.ForgotPasswordRequest;
import com.futbolin.api.dto.LoginRequest;
import com.futbolin.api.dto.RefreshRequest;
import com.futbolin.api.dto.RegisterRequest;
import com.futbolin.api.dto.ResetPasswordRequest;
import com.futbolin.api.dto.SocialLoginRequest;
import com.futbolin.api.dto.TokenResponse;
import com.futbolin.application.auth.AuthService;
import com.futbolin.core.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null) {
            authService.logout(principal.id());
        }
    }

    @PostMapping("/forgot-password")
    public void forgot(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
    }

    @PostMapping("/reset-password")
    public void reset(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
    }

    @PostMapping("/social")
    public TokenResponse social(@Valid @RequestBody SocialLoginRequest request) {
        return authService.socialLogin(request);
    }
}
