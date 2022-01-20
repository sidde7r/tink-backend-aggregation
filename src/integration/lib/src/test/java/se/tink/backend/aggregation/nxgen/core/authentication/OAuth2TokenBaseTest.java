package se.tink.backend.aggregation.nxgen.core.authentication;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class OAuth2TokenBaseTest {

    @Test
    @Parameters(method = "getTokenParameters")
    public void shouldDetermineIfTokenHasExpired(
            long expiresInSeconds, long issuedAt, boolean expectedBoolean) {
        assertEquals(
                expectedBoolean, getOAuth2Token(expiresInSeconds, issuedAt).canUseAccessToken());
    }

    private Object[] getTokenParameters() {
        return new Object[] {
            new Object[] {
                1000L, System.currentTimeMillis() / 1000L, true
            }, // testcase 1: token not expired. does not exceed 10% of token lifetime
            new Object[] {
                4000L, System.currentTimeMillis() / 1000L, true
            }, // testcase 2: token not expired. does not exceed 5 minute threshold
            new Object[] {
                1000L, (System.currentTimeMillis() / 1000L) - 901L, false
            }, // testcase 3: token expired. does exceed 10% of token lifetime
            new Object[] {
                4000L, (System.currentTimeMillis() / 1000L) - 3701L, false
            } // testcase 4: token expired. does exceed 5 minute threshold
        };
    }

    private OAuth2Token getOAuth2Token(long expiresInSeconds, long issuedAt) {
        return new OAuth2Token(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                expiresInSeconds,
                anyLong(),
                issuedAt);
    }
}
