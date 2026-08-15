package com.futbolin.application.auth;

import com.futbolin.domain.user.AuthProvider;

public interface SocialTokenVerifier {
    SocialIdentity verify(AuthProvider provider, String idToken);
}
