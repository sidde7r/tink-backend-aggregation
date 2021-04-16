package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

public interface BankIdIframeAuthenticator {

    /**
     * Returns an url that, when called from WebDriver, indicates that we've successfully completed
     * the BankID iframe authentication and we can terminate WebDriver & Proxy soon. Note that we
     * only want to detect that such request occurred and save it's response - we don't block this
     * request or any following requests that manage to execute before WebDriver & Proxy
     * termination. This is the easiest way to stop iframe authentication and finish the rest of it
     * in agent. Otherwise we would have to inject some JS interceptors, which is a separate task
     * for each bank.
     */
    String getSubstringOfUrlIndicatingAuthenticationFinish();

    void handleBankIdAuthenticationResult(BankIdIframeAuthenticationResult authenticationResult);
}
