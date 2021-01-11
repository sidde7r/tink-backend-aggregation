package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel.NEM_ID_IFRAME_AUTH_METRIC;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdCollectTokenStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdInitializeIframeStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdLoginPageStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdVerifyLoginResponseStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdWaitForCodeAppResponseStep;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
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

    public String doLoginWith(Credentials credentials) {
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
