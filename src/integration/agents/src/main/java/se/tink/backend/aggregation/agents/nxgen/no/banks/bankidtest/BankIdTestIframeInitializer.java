package se.tink.backend.aggregation.agents.nxgen.no.banks.bankidtest;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeFirstStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;

public class BankIdTestIframeInitializer implements BankIdIframeInitializer {

    @Override
    public BankIdIframeFirstStep initializeIframe(BankIdWebDriver webDriver) {
        /*
         * Examples
         *
         * DNB, RE based on bank's website:
         * - open Bank's website
         * - click Login button
         * - enter SSN
         * - click submit
         * - page reloads and now has a BankID iframe embedded
         * - since we already entered SSN, DNB initializes iframe with SSN as a parameter so the first iframe
         *   step is already some preselected default BankID method
         *
         * Nordea, RE based on mobile app:
         * - generate some random state, nonce, codeChallenge, etc.
         * - store generated values in persistent storage - they will be required to finish authentication with OAuth later
         * - create and open authentication url using generated values
         * - select BankID method from all possible authentication methods
         * - page reloads and now has a BankID iframe embedded
         * - Nordea doesn't initialize iframe with SSN parameter, so the first iframe step is to enter SSN
         */
        return BankIdIframeFirstStep.AUTHENTICATE_WITH_DEFAULT_2FA_METHOD;
    }
}
