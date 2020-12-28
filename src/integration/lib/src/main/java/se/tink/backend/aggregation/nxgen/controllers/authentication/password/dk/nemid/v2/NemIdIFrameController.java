package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.metrics.NemIdMetricLabel.NEM_ID_IFRAME_AUTH_METRIC;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps.NemIdCollectTokenStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps.NemIdInitializeIframeStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps.NemIdLoginPageStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps.NemIdVerifyLoginResponseStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps.NemIdWaitForCodeAppResponseStep;

@Slf4j
@RequiredArgsConstructor
public class NemIdIFrameController {
    // NemId Javascript Client Integration for mobile:
    // https://www.nets.eu/dk-da/kundeservice/nemid-tjenesteudbyder/NemID-tjenesteudbyderpakken/Documents/NemID%20Integration%20-%20Mobile.pdf

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdMetrics metrics;

    private final NemIdInitializeIframeStep initializeIframeStep;
    private final NemIdLoginPageStep loginPageStep;
    private final NemIdVerifyLoginResponseStep verifyLoginResponseStep;
    private final NemIdWaitForCodeAppResponseStep waitForCodeAppResponseStep;
    private final NemIdCollectTokenStep collectTokenStep;

    public String doLoginWith(Credentials credentials) throws AuthenticationException {
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
            verifyLoginResponseStep.validateLoginResponse(credentials);

            waitForCodeAppResponseStep.sendCodeAppRequestAndWaitForResponse(credentials);
            return collectTokenStep.collectToken();

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
