package se.tink.backend.aggregation.nxgen.core.authentication;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.BeforeClass;
import org.junit.Test;

public class JWTUtilsTest {
    private static final Map<String, Object> HEADER = new HashMap<>();
    private static final Algorithm ALGORITHM = hmac256("secret");

    private static final long FALLBACK = -101010101010101010L;

    @BeforeClass
    public static void setUp() {
        HEADER.put("alg", "hs256");
        HEADER.put("typ", "JWT");
    }

    @Test
    public void testExtractIssuedAtSeconds() {
        long claimIatSeconds = 1609459200L;
        long claimIatMs = claimIatSeconds * 1000L;
        String jwt =
                JWT.create()
                        .withHeader(HEADER)
                        .withClaim("iat", new Date(claimIatMs))
                        .sign(ALGORITHM);

        long extractedIatSeconds = JWTUtils.extractIssuedAtSeconds(jwt, FALLBACK);
        assertThat(extractedIatSeconds).isEqualTo(claimIatSeconds);
    }

    @Test
    public void testExpiresIssuedAtSecondsWithFallback() {
        String jwt = JWT.create().withHeader(HEADER).sign(ALGORITHM); // no payload
        long extractedIat = JWTUtils.extractIssuedAtSeconds(jwt, FALLBACK);
        assertThat(extractedIat).isEqualTo(FALLBACK);
    }

    @Test
    public void testExpiresInSeconds() {
        String jwt =
                JWT.create().withHeader(HEADER).withClaim("exp", yearFromNow()).sign(ALGORITHM);
        long expiresInSeconds = JWTUtils.calculateExpiresInSeconds(jwt, -1);
        assertThat(expiresInSeconds).isGreaterThan(0);
    }

    @Test
    public void testExpiresInSecondsWithFallback() {
        String jwt = JWT.create().withHeader(HEADER).sign(ALGORITHM); // no payload
        long extractedExp = JWTUtils.calculateExpiresInSeconds(jwt, FALLBACK);
        assertThat(extractedExp).isEqualTo(FALLBACK);
    }

    private Date yearFromNow() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, 1);
        return calendar.getTime();
    }

    @SneakyThrows
    private static Algorithm hmac256(String secret) {
        return Algorithm.HMAC256(secret);
    }
}
