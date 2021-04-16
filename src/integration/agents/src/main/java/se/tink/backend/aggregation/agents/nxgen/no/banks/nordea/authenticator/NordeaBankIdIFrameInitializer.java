package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.HtmlLocators.LOC_BANK_ID_METHOD_BUTTON;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoStorage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.AuthenticationsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.AuthenticationClient;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeFirstWindow;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;

@RequiredArgsConstructor
public class NordeaBankIdIFrameInitializer implements BankIdIframeInitializer {

    private final AuthenticationClient authenticationClient;
    private final NordeaNoStorage storage;
    private final RandomValueGenerator randomValueGenerator;

    @Override
    public BankIdIframeFirstWindow initializeIframe(BankIdWebDriver webDriver) {
        String initializeBankIdUrl = prepareUrlToInitializeBankId();

        webDriver.getUrl(initializeBankIdUrl);
        webDriver.clickButton(LOC_BANK_ID_METHOD_BUTTON);

        return BankIdIframeFirstWindow.ENTER_SSN;
    }

    private String prepareUrlToInitializeBankId() {
        String state = randomValueGenerator.generateRandomBase64UrlEncoded(26);
        String nonce = randomValueGenerator.generateRandomBase64UrlEncoded(26);
        String codeVerifier = randomValueGenerator.generateRandomBase64UrlEncoded(86);
        String codeChallenge = calculateCodeChallenge(codeVerifier);

        storage.storeCodeVerifier(codeVerifier);

        AuthenticationsResponse authenticationResponse =
                authenticationClient.getNordeaSessionDetails(codeChallenge, state, nonce);
        String sessionId = authenticationResponse.getSessionId();

        return authenticationClient.constructUrlForBankIdAuthentication(
                codeChallenge,
                state,
                nonce,
                authenticationResponse.getBankidIntegrationUrl(),
                sessionId);
    }

    private String calculateCodeChallenge(String codeVerifier) {
        return EncodingUtils.encodeAsBase64UrlSafe(Hash.sha256(codeVerifier));
    }
}
