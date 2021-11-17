package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants.NOT_OK_KEY;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants.NOT_OK_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants.SANDBOX_CODE_CHALLENGE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants.STATE;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaStorage;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@AllArgsConstructor
public class SpardaRedirectUrlBuilder {
    private final RandomValueGenerator randomValueGenerator;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SpardaStorage storage;

    public URL buildPaymentAuthorizeUrl(String redirectUrl) {
        return buildUrlWithCodeChallenge(redirectUrl);
    }

    public URL buildRedirectUrlNotOk(String redirectUrl) {
        return buildRedirectWithState(redirectUrl).queryParam(NOT_OK_KEY, NOT_OK_VALUE);
    }

    public URL buildRedirectWithState(String redirectUrl) {
        return new URL(redirectUrl).queryParam(STATE, strongAuthenticationState.getState());
    }

    public URL buildUrlWithCodeChallenge(String url) {
        return new URL(
                url.replace(
                        SpardaConstants.SANDBOX ? SANDBOX_CODE_CHALLENGE : "{code_challenge}",
                        getCodeChallange()));
    }

    private String getCodeChallange() {
        String codeVerifier = generateCodeVerifier();
        storage.saveCodeVerifier(codeVerifier);

        return Psd2Headers.generateCodeChallenge(codeVerifier);
    }

    private String generateCodeVerifier() {
        return randomValueGenerator.generateRandomAlphanumeric(60);
    }
}
