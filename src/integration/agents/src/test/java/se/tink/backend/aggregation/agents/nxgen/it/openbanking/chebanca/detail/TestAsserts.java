package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@Ignore
public class TestAsserts {

    public static void assertValid(OAuth2Token token) {
        assertNotNull(token);
        assertTrue(token.isValid());
        assertTrue(token.hasRefreshExpire());
        assertFalse(token.hasAccessExpired());
        assertTrue(token.canRefresh());
        assertNotNull(token.getRefreshToken().get());
    }

    public static void assertEqual(OAuth2Token expected, OAuth2Token given) {
        assertEquals(expected.getAccessToken(), given.getAccessToken());
        assertEquals(expected.getRefreshToken(), given.getRefreshToken());
        assertEquals(expected.getTokenType(), given.getTokenType());
        assertEquals(expected.getAccessExpireEpoch(), given.getAccessExpireEpoch());
        assertEquals(expected.getRefreshExpireEpoch(), given.getRefreshExpireEpoch());
        assertEquals(expected.isValid(), expected.isValid());
    }
}
