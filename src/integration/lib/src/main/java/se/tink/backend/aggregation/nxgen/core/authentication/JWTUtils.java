package se.tink.backend.aggregation.nxgen.core.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import java.util.Date;

public class JWTUtils {
    public static long extractIssuedAtSeconds(String jwt, long fallback) {
        try {
            Date dateIssuedAt = JWT.decode(jwt).getIssuedAt();
            if (dateIssuedAt == null) {
                return fallback;
            } else {
                return dateIssuedAt.getTime() / 1000L;
            }
        } catch (JWTDecodeException e) {
            return fallback;
        }
    }

    public static long calculateExpiresInSeconds(String jwt, long fallback) {
        try {
            Date dateExpiresAt = JWT.decode(jwt).getExpiresAt();
            if (dateExpiresAt == null) {
                return fallback;
            } else {
                long currentEpochMs = System.currentTimeMillis();
                long expiresAtMs = dateExpiresAt.getTime();
                return (expiresAtMs - currentEpochMs) / 1000L;
            }
        } catch (JWTDecodeException e) {
            return fallback;
        }
    }
}
