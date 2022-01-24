package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.MIT_ID_LOG_TAG;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.WaitTime.WAIT_FOR_FIRST_AUTHENTICATION_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.WaitTime.WAIT_TO_CHECK_IF_AUTH_FINISHED;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.WaitTime.WAIT_TO_CHECK_IF_USER_HAS_TO_ENTER_CPR;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.WaitTime.WAIT_TO_GIVE_CPR_SCREEN_TIME_TO_EXIT;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.mitid.MitIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdAuthenticationResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreensQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.MitId2FAStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.MitIdEnterCprStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.MitIdUserIdStep;
import se.tink.integration.webdriver.service.WebDriverService;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MitIdFlowController {

    private final WebDriverService driverService;

    private final MitIdScreensManager screensManager;
    private final MitIdProxyListenersRegistry listenersRegistry;
    private final MitIdAuthFinishProxyListener authFinishProxyListener;
    private final MitIdAuthenticator mitIdAuthenticator;

    private final MitIdUserIdStep userIdStep;
    private final MitId2FAStep secondFactorStep;
    private final MitIdEnterCprStep enterCprStep;

    /** Register all proxy listeners required in MitID authentication flow */
    public void registerProxyListeners() {
        log.info("{} Registering proxy listeners", MIT_ID_LOG_TAG);
        listenersRegistry.registerListeners();
    }

    /** Conduct the whole MitID flow */
    public MitIdAuthenticationResult authenticate() {
        MitIdScreen firstScreen = waitForFirstAuthenticationScreen();

        if (firstScreen == MitIdScreen.USER_ID_SCREEN) {
            userIdStep.enterUserId();
        }
        secondFactorStep.perform2FA();

        boolean shouldEnterCpr = hasToEnterCpr();
        log.info("{} Should enter cpr: {}", MIT_ID_LOG_TAG, shouldEnterCpr);
        if (shouldEnterCpr) {
            enterCprStep.enterCpr();
        }

        return waitForAuthenticationResult(shouldEnterCpr);
    }

    private MitIdScreen waitForFirstAuthenticationScreen() {
        MitIdScreen screen =
                screensManager.searchForFirstScreen(
                        MitIdScreensQuery.builder()
                                .searchForExpectedScreens(MitIdScreen.USER_ID_SCREEN)
                                .searchForExpectedScreens(MitIdScreen.SECOND_FACTOR_SCREENS)
                                .searchForSeconds(WAIT_FOR_FIRST_AUTHENTICATION_SCREEN)
                                .build());
        log.info("{} First screen detected: {}", MIT_ID_LOG_TAG, screen);
        return screen;
    }

    private boolean hasToEnterCpr() {
        for (int i = 0; i < WAIT_TO_CHECK_IF_USER_HAS_TO_ENTER_CPR; i++) {
            boolean authFinished = authFinishProxyListener.hasResponse();
            if (authFinished) {
                log.warn(
                        "{} Authentication finished with proxy response before CPR",
                        MIT_ID_LOG_TAG);
                return false;
            }

            boolean authFinishedAbnormally =
                    mitIdAuthenticator.isAuthenticationFinishedAbnormally(driverService);
            if (authFinishedAbnormally) {
                log.warn("{} Authentication finished abnormally before CPR", MIT_ID_LOG_TAG);
                mitIdAuthenticator.handleAuthenticationFinishedAbnormally(driverService);
            }

            if (isOnCprScreen()) {
                return true;
            }
            driverService.sleepFor(1_000);
        }

        // search one more time for CPR, this time throw a screen not found error
        screensManager.searchForFirstScreen(
                MitIdScreensQuery.builder()
                        .searchForExpectedScreens(MitIdScreen.CPR_SCREEN)
                        .searchOnlyOnce()
                        .build());
        return true;
    }

    private MitIdAuthenticationResult waitForAuthenticationResult(boolean wasOnCprScreen) {
        for (int i = 0; i < WAIT_TO_CHECK_IF_AUTH_FINISHED; i++) {

            boolean authFinished = authFinishProxyListener.hasResponse();
            if (authFinished) {
                log.warn(
                        "{} Authentication finished with proxy response after CPR", MIT_ID_LOG_TAG);
                return MitIdAuthenticationResult.builder()
                        .proxyResponse(authFinishProxyListener.getResponse())
                        .driverService(driverService)
                        .build();
            }

            boolean authFinishedAbnormally =
                    mitIdAuthenticator.isAuthenticationFinishedAbnormally(driverService);
            if (authFinishedAbnormally) {
                log.warn("{} Authentication finished abnormally after CPR", MIT_ID_LOG_TAG);
                mitIdAuthenticator.handleAuthenticationFinishedAbnormally(driverService);
                break;
            }

            if (wasOnCprScreen && i > WAIT_TO_GIVE_CPR_SCREEN_TIME_TO_EXIT && isOnCprScreen()) {
                throw MitIdError.INVALID_CPR.exception();
            }

            driverService.sleepFor(1_000);
        }

        throw new IllegalStateException("Did not receive auth finish response from proxy");
    }

    private boolean isOnCprScreen() {
        return screensManager
                .trySearchForFirstScreen(
                        MitIdScreensQuery.builder()
                                .searchForExpectedScreens(MitIdScreen.CPR_SCREEN)
                                .searchOnlyOnce()
                                .build())
                .isPresent();
    }
}
