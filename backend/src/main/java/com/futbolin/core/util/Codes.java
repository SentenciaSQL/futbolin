package com.futbolin.core.util;

import java.security.SecureRandom;
import java.util.Locale;

public final class Codes {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private Codes() {}

    public static String privateMatchCode() {
        return "FUT-" + random(4);
    }

    public static String random(int length) {
        char[] buf = new char[length];
        for (int i = 0; i < length; i++) {
            buf[i] = ALPHANUM[RANDOM.nextInt(ALPHANUM.length)];
        }
        return new String(buf);
    }

    public static String normalizeUsername(String username) {
        return username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
