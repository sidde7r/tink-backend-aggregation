package se.tink.aggregation.lib.src.test.java.se.tink.backend.aggregation.nxgen.core.authentication;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class OAuth2TokenTest {

    private static final String DUMMY_TOKEN_TYPE = "Bearer";
    private static final String DUMMY_ACCESS_TOKEN = "DUMMY_ACCESS_TOKEN";
    private static final String DUMMY_REFRESH_TOKEN = "DUMMY_REFRESH_TOKEN";
    private static final String DUMMY_ID_TOKEN = "DUMMY_ID_TOKEN";
    private static final long DUMMY_ACCESS_EXPIRES_IN_SECONDS = 300L;
    private static final long DUMMY_REFRESH_EXPIRES_IN_SECONDS = 7776000L;
    private static final long DUMMY_ISSUED_AT = 0L;

    @Test
    public void shouldReturnMaskingFailedMessage() {
        // given
        OAuth2Token oAuth2Token =
                new OAuth2Token(
                        DUMMY_TOKEN_TYPE,
                        DUMMY_ACCESS_TOKEN,
                        DUMMY_REFRESH_TOKEN,
                        DUMMY_ID_TOKEN,
                        DUMMY_ACCESS_EXPIRES_IN_SECONDS,
                        DUMMY_REFRESH_EXPIRES_IN_SECONDS,
                        DUMMY_ISSUED_AT);

        // when
        String unmaskedTokenDetails = oAuth2Token.toMaskedString(null);

        // then
        assertThat(unmaskedTokenDetails).isEqualTo("Masking token failed");
    }

    @Test
    public void shouldNotThrowNullPointerException() {
        // given
        OAuth2Token oAuth2Token =
                new OAuth2Token(
                        null,
                        null,
                        null,
                        null,
                        DUMMY_ACCESS_EXPIRES_IN_SECONDS,
                        DUMMY_REFRESH_EXPIRES_IN_SECONDS,
                        DUMMY_ISSUED_AT);

        // when
        String unmaskedTokenDetails = oAuth2Token.toMaskedString(new LogMaskerImpl());

        // then
        assertThat(unmaskedTokenDetails)
                .isEqualTo("Masking not applied correctly. Hiding the output.");
    }

    @Test
    public void shouldHideOutputEvenIfMaskingIsOff() {
        // given
        OAuth2Token oAuth2Token =
                new OAuth2Token(
                        DUMMY_TOKEN_TYPE,
                        DUMMY_ACCESS_TOKEN,
                        DUMMY_REFRESH_TOKEN,
                        DUMMY_ID_TOKEN,
                        DUMMY_ACCESS_EXPIRES_IN_SECONDS,
                        DUMMY_REFRESH_EXPIRES_IN_SECONDS,
                        DUMMY_ISSUED_AT);

        // when
        String unmaskedTokenDetails = oAuth2Token.toMaskedString(new FakeLogMasker());

        // then
        assertThat(unmaskedTokenDetails)
                .isEqualTo("Masking not applied correctly. Hiding the output.");
    }

    @Test
    public void shouldReturnMaskedTokenDetails() {
        // given
        OAuth2Token oAuth2Token =
                new OAuth2Token(
                        DUMMY_TOKEN_TYPE,
                        DUMMY_ACCESS_TOKEN,
                        DUMMY_REFRESH_TOKEN,
                        DUMMY_ID_TOKEN,
                        DUMMY_ACCESS_EXPIRES_IN_SECONDS,
                        DUMMY_REFRESH_EXPIRES_IN_SECONDS,
                        DUMMY_ISSUED_AT);

        // when
        String maskedTokenDetails = oAuth2Token.toMaskedString(new LogMaskerImpl());

        // then
        assertThat(maskedTokenDetails)
                .isEqualTo(
                        "OAuth2Token{"
                                + "tokenType = Bearer, "
                                + "accessToken = **HASHED:Qz**, "
                                + "refreshToken = **HASHED:F7**, "
                                + "idToken = **HASHED:R9**, "
                                + "expiresInSeconds = 300 [1970-01-01T00:05], "
                                + "refreshExpiresInSeconds = 7776000 [1970-04-01T00:00], "
                                + "issuedAt = 0 [1970-01-01T00:00]}");
    }
}
