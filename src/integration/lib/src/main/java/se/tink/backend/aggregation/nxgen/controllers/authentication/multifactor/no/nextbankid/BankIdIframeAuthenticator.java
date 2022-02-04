package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import se.tink.integration.webdriver.service.proxy.ProxySaveResponseMatcher;

public interface BankIdIframeAuthenticator {

    /**
     * During BankID authentication, both BankID iframe and it's parent page make several HTTP
     * requests. At some point, there has to be a distinctive request that finishes BankID
     * authentication - as it's response, typically we get some kind of an authentication code that
     * we can later exchange with bank's API for authentication token for bank's mobile app (which
     * we simulate by our Agent).
     *
     * <p>This method should return a matcher that will detect a response for said request. When we
     * detect it, it means that we've successfully completed the BankID iframe authentication and we
     * can terminate WebDriver & Proxy soon.
     *
     * <p>NOTE: we only want to detect that such request occurred and save it's response - we don't
     * block this request or any following requests that manage to execute before WebDriver & Proxy
     * are terminated. This is the easiest & most universal way to stop iframe authentication and
     * finish the rest of it in Agent. Otherwise we would have to inject some JS interceptors, which
     * is a separate task for each bank.
     */
    ProxySaveResponseMatcher getProxyResponseMatcherToDetectAuthenticationWasFinished();

    void handleBankIdAuthenticationResult(BankIdIframeAuthenticationResult authenticationResult);
}
