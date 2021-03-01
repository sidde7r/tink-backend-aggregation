package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codeapp;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel.WAITING_FOR_SUPPLEMENTAL_INFO_METRIC;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.utils.supplementalfields.DanishFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
class NemIdCodeAppAskUserToApproveRequestStep {

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdMetrics metrics;
    private final NemIdCredentialsStatusUpdater statusUpdater;
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    void sendCodeAppRequestAndWaitForResponse(Credentials credentials) {
        metrics.executeWithTimer(
                () -> {
                    sendNemIdCodeAppApprovalRequest();
                    displayPromptToOpenNemIdAppAndWaitForUserResponse(credentials);
                },
                WAITING_FOR_SUPPLEMENTAL_INFO_METRIC);
    }

    private void sendNemIdCodeAppApprovalRequest() {
        driverWrapper.clickButton(SUBMIT_BUTTON);
        log.info("{} NemId code app request sent", NEM_ID_PREFIX);
    }

    private void displayPromptToOpenNemIdAppAndWaitForUserResponse(Credentials credentials) {
        Field field = DanishFields.NemIdInfo.build(catalog);

        statusUpdater.updateStatusPayload(
                credentials, UserMessage.OPEN_NEM_ID_APP_AND_CLICK_BUTTON);

        try {
            supplementalInformationController.askSupplementalInformationSync(field);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }
}
