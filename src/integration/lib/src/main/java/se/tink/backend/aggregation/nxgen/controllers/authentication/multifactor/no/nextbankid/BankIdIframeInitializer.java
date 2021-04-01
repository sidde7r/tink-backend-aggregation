package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;

/**
 * BankID iframe can only be embedded on preregistered domains i.e. some bank's website address -
 * otherwise the iframe's JS detects invalid domain and throws an error. Because of this fact, the
 * easiest way to get an initialized iframe is to just open bank's authentication page and do a
 * little bit of screen scraping to open iframe there. This process is different for every bank and
 * the iframe itself may be embedded in different websites (different parent windows) but its
 * content is universal everywhere.
 *
 * <p>Initialization typically requires user to enter SSN (Social Security Number) and then select
 * one of possible authentication methods, e.g.:
 *
 * <ul>
 *   <li>BankID (which handles multiple existing BankID methods by embedding one universal iframe -
 *       that's our case and we need to choose it)
 *   <li>BankID on mobile (which is a single BankID method on its own and requires UI to be
 *       implemented by every bank separately)
 *   <li>others, e.g. DNB's password calculator
 * </ul>
 */
public interface BankIdIframeInitializer {

    /**
     * Navigate WebDriver to a bank's page & interact with it to embed BankID iframe.
     *
     * @param webDriver - WebDriver which has to be used in the initialization process - it will
     *     later be reused to continue user authentication with an iframe. This way all possibly
     *     required cookies will automatically be ready for next steps.
     * @return - what's the first expected authentication window in the BankID iframe that will be
     *     open. Typically there are only 2 options - either user will see a window to enter their
     *     SSN or, if they did it already during initialization process (on bank's website), a
     *     window to authenticate with their default BankID method straight away.
     */
    BankIdIframeFirstWindow initializeIframe(BankIdWebDriver webDriver);
}
