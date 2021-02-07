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
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdVerifyLoginResponseStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codeapp.NemIdAuthorizeWithCodeAppStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codecard.NemIdAuthorizeWithCodeCardStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;

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

    private final NemIdAuthorizeWithCodeAppStep authorizeWithCodeAppStep;
    private final NemIdAuthorizeWithCodeCardStep authorizeWithCodeCardStep;

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

            NemId2FAMethod available2FAMethod =
                    verifyLoginResponseStep.checkLoginResultAndGetAvailable2FAMethod(credentials);
            String tokenBase64 = getNemIdToken(available2FAMethod, credentials);

            tokenValidator.verifyTokenIsValid(tokenBase64);
            return tokenBase64;

        } catch (Exception e) {
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

    private String getNemIdToken(NemId2FAMethod nemId2FAMethod, Credentials credentials) {
        switch (nemId2FAMethod) {
            case CODE_APP:
                return authorizeWithCodeAppStep.getNemIdTokenWithCodeAppAuth(credentials);
            case CODE_CARD:
                return authorizeWithCodeCardStep.getNemIdTokenWithCodeCardAuth(credentials);
            case CODE_TOKEN:
                throw NemIdError.CODE_TOKEN_NOT_SUPPORTED.exception(
                        NEM_ID_PREFIX + " User has code token.");
            default:
                throw new IllegalStateException("Unknown NemId 2FA method");
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
