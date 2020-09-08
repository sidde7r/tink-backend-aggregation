package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@Slf4j
public class OAuth2TokenEnricher {

    public static void enrich(OAuth2Token accessToken) {
        if (accessToken.getTokenType() == null) {
            log.info("Missing token type in response, adding Bearer as token type.");
            accessToken.setTokenType("Bearer");
        }
    }
}
