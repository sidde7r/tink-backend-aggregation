package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel.NEM_ID_IFRAME_AUTH_METRIC;

import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdInitializeIframeStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdLoginPageStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdPerform2FAStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdVerifyLoginResponseStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdChoose2FAMethodStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor = @__({@Inject}))
public class NemIdIFrameController {
    // NemId Javascript Client Integration for mobile:
    // https://www.nets.eu/dk-da/kundeservice/nemid-tjenesteudbyder/NemID-tjenesteudbyderpakken/Documents/NemID%20Integration%20-%20Mobile.pdf

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdMetrics metrics;
    private final NemIdTokenValidator tokenValidator;

    private final NemIdInitializeIframeStep initializeIframeStep;
    private final NemIdLoginPageStep loginPageStep;
    private final NemIdVerifyLoginResponseStep verifyLoginResponseStep;
    private final NemIdChoose2FAMethodStep choose2FAMethodStep;
    private final NemIdPerform2FAStep perform2FAStep;

    public String logInWithCredentials(Credentials credentials) {
        try {
            return metrics.executeWithTimer(() -> doLogin(credentials), NEM_ID_IFRAME_AUTH_METRIC);
        } finally {
            logTimeMetrics();
        }
    }

    private String doLogin(Credentials credentials) {
        try {
            initializeIframeStep.initializeNemIdIframe(credentials);
            loginPageStep.login(credentials);

            NemId2FAMethodScreen default2FAScreen =
                    verifyLoginResponseStep.checkLoginResultAndGetDefault2FAScreen(credentials);
            NemId2FAMethod userSelected2FAMethod =
                    choose2FAMethodStep.choose2FAMethod(credentials, default2FAScreen);

            String tokenBase64 =
                    perform2FAStep.authenticateToGetNemIdToken(userSelected2FAMethod, credentials);

            tokenValidator.verifyTokenIsValid(tokenBase64);
            return tokenBase64;

        } catch (RuntimeException e) {
            log.error(
                    "{} NemId authentication error: {}\n{}",
                    NEM_ID_PREFIX,
                    e.getMessage(),
                    driverWrapper.getFullPageSourceLog(),
                    e);
            throw e;
        } finally {
            driverWrapper.quitDriver();
        }
    }

    private void logTimeMetrics() {
        metrics.saveTimeMetrics();
        log.info(
                "{} NemId iframe authentication took (s):\n{}",
                NEM_ID_PREFIX,
                metrics.getTimersLog());
    }
}
