package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocatorsElements;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.proxy.ProxySaveResponseMatcher;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;

public interface MitIdAuthenticator {

    /**
     * Using already running {@link WebDriverService}, initialize authentication to the point when
     * there is a MitID window open in WebDriver's browser.
     */
    void initializeMitIdWindow(WebDriverService driverService);

    /**
     * Using this method, you can customize the exact {@link ElementLocator} that will be used to
     * find the element marked by specific {@link MitIdLocator}.
     */
    MitIdLocatorsElements getLocatorsElements();

    /**
     * Define a matcher looking for proxy response that will contain some authorization code which
     * we can then exchange for authorization tokens.
     */
    ProxySaveResponseMatcher getMatcherForAuthenticationFinishResponse();

    /** Use authentication result to complete the whole agent's authorization process. */
    void finishAuthentication(MitIdAuthenticationResult authenticationResult);

    /**
     * Allows agent to look for its own specific signs that authentication has finished with an
     * error, e.g. a distinct error screen.
     */
    default boolean isAuthenticationFinishedWithAgentSpecificError(WebDriverService driverService) {
        return false;
    }

    /**
     * Allows agent to throw appropriate exception when it detects that authentication has finished
     * with agent specific error.
     */
    default void handleAuthenticationFinishedWithAgentSpecificError(
            WebDriverService driverService) {}
}
