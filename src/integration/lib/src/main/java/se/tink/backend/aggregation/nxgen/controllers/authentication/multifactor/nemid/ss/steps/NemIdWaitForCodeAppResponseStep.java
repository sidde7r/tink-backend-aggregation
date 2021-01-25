package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_APP_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel.WAITING_FOR_SUPPLEMENTAL_INFO_METRIC;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.utils.supplementalfields.DanishFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
@RequiredArgsConstructor
public class NemIdWaitForCodeAppResponseStep {

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdMetrics metrics;
    private final NemIdCredentialsStatusUpdater statusUpdater;
    private final Catalog catalog;
    private final SupplementalRequester supplementalRequester;

    public void sendCodeAppRequestAndWaitForResponse(Credentials credentials) {
        metrics.executeWithTimer(
                () -> {
                    sendNemIdCodeAppApprovalRequest();
                    displayPromptToOpenNemIdAppAndWaitForUserResponse(credentials);
                },
                WAITING_FOR_SUPPLEMENTAL_INFO_METRIC);
    }

    private void sendNemIdCodeAppApprovalRequest() {
        driverWrapper.clickButton(NEMID_CODE_APP_BUTTON);
        log.info("{} NemId code app request sent", NEM_ID_PREFIX);
    }

    private void displayPromptToOpenNemIdAppAndWaitForUserResponse(Credentials credentials) {
        statusUpdater.updateStatusPayload(
                credentials, UserMessage.OPEN_NEM_ID_APP_AND_CLICK_BUTTON);

        Field field = DanishFields.NemIdInfo.build(catalog);

        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(Collections.singletonList(field)));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);

        String supplementalInfoResponse =
                supplementalRequester.requestSupplementalInformation(
                        credentials,
                        NemIdConstants.NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN,
                        TimeUnit.SECONDS,
                        true);

        /*
         * When user doesn't click "Ok" button in app we should abandon authentication - otherwise we would
         * face issues with opt-in feature.
         */
        if (supplementalInfoResponse == null) {
            throw SupplementalInfoError.WAIT_TIMEOUT.exception();
        }
    }
}
