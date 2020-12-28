package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.IFRAME;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.USERNAME_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.metrics.NemIdMetricLabel.FETCHING_NEM_ID_PARAMETERS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.metrics.NemIdMetricLabel.WAITING_FOR_NEM_ID_IFRAME_METRIC;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.metrics.NemIdMetricLabel.WAITING_FOR_USER_INPUT_METRIC;

import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdParametersFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdParametersV2;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.metrics.NemIdMetrics;

@Slf4j
@RequiredArgsConstructor
public class NemIdInitializeIframeStep {

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdMetrics metrics;
    private final NemIdCredentialsStatusUpdater statusUpdater;
    private final NemIdParametersFetcher nemIdParametersFetcher;

    public void initializeNemIdIframe(Credentials credentials) {
        initializeNemIdIframePage();
        log.info("{} iframe is initialized", NEM_ID_PREFIX);
        statusUpdater.updateStatusPayload(credentials, UserMessage.NEM_ID_PROCESS_INIT);
    }

    private void initializeNemIdIframePage() {
        for (int i = 0; i < 5; i++) {
            NemIdParametersV2 nemIdParameters =
                    metrics.executeWithTimer(
                            nemIdParametersFetcher::getNemIdParameters, FETCHING_NEM_ID_PARAMETERS);

            // this will setup browser with values specific to NemId page, like current url, etc.
            driverWrapper.get(NemIdConstantsV2.NEM_ID_APPLET_URL);

            // create initial html to inject
            String html =
                    String.format(NemIdConstantsV2.BASE_HTML, nemIdParameters.getNemIdElements());
            String b64Html = Base64.getEncoder().encodeToString(html.getBytes());

            if (tryInitializeNemId(b64Html)) {
                return;
            }
        }
        throw new IllegalStateException(
                NEM_ID_PREFIX + " Can't instantiate iframe element with NemId form.");
    }

    private boolean tryInitializeNemId(String b64Html) {
        driverWrapper.switchToParentWindow();
        driverWrapper.executeScript("document.write(atob(\"" + b64Html + "\"));");

        boolean nemIdIframeInitialized =
                metrics.executeWithTimer(
                        () -> driverWrapper.waitForElement(IFRAME, 15).isPresent(),
                        WAITING_FOR_NEM_ID_IFRAME_METRIC);

        if (!nemIdIframeInitialized) {
            return false;
        }
        driverWrapper.trySwitchToNemIdIframe();

        return metrics.executeWithTimer(
                () -> driverWrapper.waitForElement(USERNAME_INPUT, 15).isPresent(),
                WAITING_FOR_USER_INPUT_METRIC);
    }
}
