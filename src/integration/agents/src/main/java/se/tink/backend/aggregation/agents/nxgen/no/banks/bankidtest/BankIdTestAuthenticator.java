package se.tink.backend.aggregation.agents.nxgen.no.banks.bankidtest;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticator;

@RequiredArgsConstructor
public class BankIdTestAuthenticator implements BankIdIframeAuthenticator {

    @Override
    public void autoAuthenticate() {
        throw SessionError.SESSION_EXPIRED.exception();
    }

    @Override
    public String getSubstringOfUrlIndicatingAuthenticationFinish() {
        return "https://bankid.test/authentication/finish/redirect";
    }

    @Override
    public void handleBankIdAuthenticationResult(
            BankIdIframeAuthenticationResult authenticationResult) {
        /*
         * Examples
         *
         * DNB, RE based on website scraping:
         * - get cookies from WebDriver
         * - set cookies in api client
         *
         * Nordea, RE based on mobile:
         * - get redirect url from response
         * - extract authorization code from redirect url query string
         * - read state, nonce, code challenge etc. saved in persistent storage by iframe initializer
         * - finish authentication by making a request to bank's OAuth API with authorization code & state
         */
    }
}
