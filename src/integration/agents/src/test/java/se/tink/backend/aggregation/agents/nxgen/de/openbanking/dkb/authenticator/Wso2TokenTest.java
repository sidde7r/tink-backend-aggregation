package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class Wso2TokenTest {

    @Test
    public void toOAuth2TokenShouldReturnValidOAuth2Token() {
        // given
        String givenTokenValue = "tokenValue";
        String givenTokenType = "tokenType";
        Long givenExpiresIn = 123L;

        Wso2Token tested = new Wso2Token();
        tested.setAccessToken(givenTokenValue);
        tested.setTokenType(givenTokenType);
        tested.setExpiresIn(givenExpiresIn);

        // when
        OAuth2Token result = tested.toOAuth2Token();

        // then
        assertThat(result.getTokenType()).isEqualTo(givenTokenType);
        assertThat(result.getAccessToken()).isEqualTo(givenTokenValue);
        assertThat(result.getExpiresInSeconds()).isEqualTo(givenExpiresIn);
    }
}
