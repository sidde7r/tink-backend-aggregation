package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdIframeController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.proxy.ProxyManager;
import se.tink.integration.webdriver.service.proxy.ResponseFromProxy;
import se.tink.libraries.credentials.service.UserAvailability;

/**
 * This class is responsible for executing all logic required for agent to fully authenticate using
 * BankID iframe screen scraping.
 *
 * <p>As in every screen scraping, this solution is based on {@link org.openqa.selenium.WebDriver}.
 * WebDriver simulates user interactions with BankID iframe which looks and works exactly the same
 * for all agents in NO. What's different is where this iframe is embedded - sometimes it will be
 * bank's own website, other times it could be an external service (e.g. OIDC). So, in order to make
 * a use of common driver logic, we need to initialize the iframe first, which is a separate task
 * for each agent.
 *
 * <p>Also, after BankID authentication is finished (i.e. iframe redirects user back to bank's
 * website / mobile app), agents need to obtain some kind of an authorization result and make a few
 * more requests with it to bank's API. This presents us with 2 challenges:
 *
 * <ul>
 *   <li>Recognize when BankID iframe authentication is finished. One option is to continue screen
 *       scraping to see what's visible in WebDriver and try to detect when we're already on some
 *       main page for logged in user. This however is quite slow (requires additional waiting for
 *       HTML element), can be hard to maintain and won't work well if BankID iframe is embedded on
 *       some external service that redirects user back to mobile app - we're not running on mobile
 *       device so the browser used by WebDriver will not be able to open mobile app deep link URI.
 *       Another way would be to embed some JS that will try to recognize events posted from BankID
 *       iframe but this is really difficult task to do. Finally, the best solution is to use a
 *       {@link org.openqa.selenium.Proxy} and simply detect that WebDriver made a specific request
 *       that, based on previously recorded traffic, we're sure means authentication finish.
 *   <li>
 *   <li>Get BankID authorization result. E.g. in Nordea, at the end of BankID authentication user
 *       is redirected back to mobile app with url
 *       com.nordea.mobilebank.no://auth-callback?authorization_code=XXX. What we need to do is
 *       extract this code and call Nordea's Oauth2 API with it. Again, the easiest (if not the
 *       only) way to do it is by using {@link org.openqa.selenium.Proxy}.
 *   <li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class BankIdIframeAuthenticationController
        implements Authenticator, MultiFactorAuthenticator {

    private static final int WAIT_FOR_PROXY_RESPONSE_IN_SECONDS = 10;

    private final WebDriverService webDriver;
    private final AgentTemporaryStorage agentTemporaryStorage;
    private final ProxyManager proxyManager;
    private final BankIdAuthenticationState authenticationState;
    private final BankIdIframeInitializer iframeInitializer;
    private final BankIdIframeAuthenticator iframeAuthenticator;
    private final BankIdIframeController iframeController;
    private final UserAvailability userAvailability;

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        checkIfUserIsPresent();

        try {
            setupProxyResponseListener();

            BankIdIframeFirstWindow firstIframeWindow =
                    iframeInitializer.initializeIframe(webDriver);
            authenticationState.setFirstIframeWindow(firstIframeWindow);

            iframeController.authenticateWithCredentials(credentials);

            ResponseFromProxy authFinishUrlProxyResponse = waitForAuthFinishUrlResponse();

            iframeAuthenticator.handleBankIdAuthenticationResult(
                    BankIdIframeAuthenticationResult.builder()
                            .proxyResponseFromAuthFinishUrl(authFinishUrlProxyResponse)
                            .webDriver(webDriver)
                            .build());

        } catch (RuntimeException e) {
            log.error(
                    "{} BankID iframe authentication error: {}\n{}",
                    e.getMessage(),
                    webDriver.getFullPageSourceLog(BankIdConstants.HtmlSelectors.BY_IFRAME),
                    e);
            throw e;

        } finally {
            proxyManager.shutDownProxy();
            agentTemporaryStorage.remove(webDriver.getDriverId());
        }
    }

    private void checkIfUserIsPresent() {
        if (!userAvailability.isUserPresent()) {
            throw SessionError.SESSION_EXPIRED.exception(
                    "User is not present. Fail refresh before entering user's data into BankID");
        }
    }

    private void setupProxyResponseListener() {
        String urlToListenFor =
                iframeAuthenticator.getSubstringOfUrlIndicatingAuthenticationFinish();
        proxyManager.setUrlSubstringToListenFor(urlToListenFor);
    }

    private ResponseFromProxy waitForAuthFinishUrlResponse() {
        return proxyManager
                .waitForProxyResponse(WAIT_FOR_PROXY_RESPONSE_IN_SECONDS)
                .orElseThrow(() -> new IllegalStateException("Did not found proxy response"));
    }
}
