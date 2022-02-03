package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.MIT_ID_LOG_TAG;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.Timeouts.CPR_SCREEN_EXIT_TIMEOUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.Timeouts.FIRST_AUTHENTICATION_SCREEN_SEARCH_TIMEOUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.Timeouts.GET_AUTH_RESULT_TIMEOUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.Timeouts.HANDLE_CPR_TIMEOUT;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.mitid.MitIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdAuthenticationResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreenQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.MitId2FAStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.MitIdEnterCprStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.MitIdUserIdStep;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.proxy.ProxySaveResponseFilter;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MitIdScreenFlowController {

    private final WebDriverService driverService;

    private final MitIdScreensManager screensManager;
    private final MitIdProxyFiltersRegistry filtersRegistry;
    private final ProxySaveResponseFilter authFinishProxyFilter;
    private final MitIdAuthenticator mitIdAuthenticator;

    private final MitIdUserIdStep userIdStep;
    private final MitId2FAStep secondFactorStep;
    private final MitIdEnterCprStep enterCprStep;

    public void registerProxyFilters() {
        log.info("{} Registering proxy filters", MIT_ID_LOG_TAG);
        filtersRegistry.registerFilters();
    }

    public MitIdAuthenticationResult runScreenFlow() {
        MitIdScreen firstScreen = waitForFirstAuthenticationScreen();
        if (firstScreen == MitIdScreen.USER_ID_SCREEN) {
            userIdStep.enterUserId();
        }
        secondFactorStep.perform2FA();
        handleCpr();
        return getAuthenticationResult();
    }

    private MitIdScreen waitForFirstAuthenticationScreen() {
        MitIdScreen screen =
                screensManager.searchForFirstScreen(
                        MitIdScreenQuery.builder()
                                .searchForExpectedScreens(MitIdScreen.USER_ID_SCREEN)
                                .searchForExpectedScreens(MitIdScreen.SECOND_FACTOR_SCREENS)
                                .searchForSeconds(FIRST_AUTHENTICATION_SCREEN_SEARCH_TIMEOUT)
                                .build());
        log.info("{} First screen detected: {}", MIT_ID_LOG_TAG, screen);
        return screen;
    }

    private void handleCpr() {
        for (int i = 0; i < HANDLE_CPR_TIMEOUT; i++) {
            if (isAuthFinished()) {
                log.info("Auth finished before CPR");
                return;
            }
            if (isAgentSpecificError()) {
                log.info("Agent specific error before CPR");
                return;
            }
            if (isOnCprScreen()) {
                enterCprStep.enterCpr();
                letCprExit();
                return;
            }
            driverService.sleepFor(1_000);
        }
        throw new IllegalStateException("Authentication not finished and no CPR screen");
    }

    private MitIdAuthenticationResult getAuthenticationResult() {
        for (int i = 0; i < GET_AUTH_RESULT_TIMEOUT; i++) {
            if (isAuthFinished()) {
                return MitIdAuthenticationResult.builder()
                        .proxyResponse(authFinishProxyFilter.getResponse())
                        .driverService(driverService)
                        .build();
            }
            if (isAgentSpecificError()) {
                mitIdAuthenticator.handleAuthenticationFinishedWithAgentSpecificError(
                        driverService);
                throw new IllegalStateException("Unhandled agent specific error");
            }
            driverService.sleepFor(1_000);
        }
        throw new IllegalStateException("Did not receive auth finish response from proxy");
    }

    private boolean isAuthFinished() {
        return authFinishProxyFilter.hasResponse();
    }

    private boolean isAgentSpecificError() {
        return mitIdAuthenticator.isAuthenticationFinishedWithAgentSpecificError(driverService);
    }

    private void letCprExit() {
        for (int i = 0; i < CPR_SCREEN_EXIT_TIMEOUT; i++) {
            if (!isOnCprScreen()) {
                return;
            }
            driverService.sleepFor(1_000);
        }
        throw MitIdError.INVALID_CPR.exception();
    }

    private boolean isOnCprScreen() {
        return screensManager
                .trySearchForFirstScreen(
                        MitIdScreenQuery.builder()
                                .searchForExpectedScreens(MitIdScreen.CPR_SCREEN)
                                .searchOnlyOnce()
                                .build())
                .isPresent();
    }
}
