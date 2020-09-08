package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class OAuth2TokenEnricherTest {

    @Test
    public void enrichShouldNotChangeToken() {
        // given
        OAuth2Token token = OAuth2Token.create("token type", "access token", "refresh token", 5000);

        // when
        OAuth2TokenEnricher.enrich(token);

        // then
        assertThat(token.getTokenType()).isEqualTo("token type");
    }

    @Test
    public void enrichShouldSetTokenTypeBearerWhenItIsMissing() {
        // given
        OAuth2Token token = new OAuth2Token();

        // when
        OAuth2TokenEnricher.enrich(token);

        // then
        assertThat(token.getTokenType()).isEqualTo("Bearer");
    }
}
