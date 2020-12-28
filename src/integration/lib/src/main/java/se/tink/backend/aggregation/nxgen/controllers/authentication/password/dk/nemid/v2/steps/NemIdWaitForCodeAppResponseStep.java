package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.NEMID_CODE_APP_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.metrics.NemIdMetricLabel.WAITING_FOR_SUPPLEMENTAL_INFO_METRIC;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.utils.supplementalfields.DanishFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.metrics.NemIdMetrics;
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
                    sendNemIdApprovalNotification();
                    displayPromptToOpenNemIdApp(credentials);
                },
                WAITING_FOR_SUPPLEMENTAL_INFO_METRIC);
    }

    private void sendNemIdApprovalNotification() {
        driverWrapper.clickButton(NEMID_CODE_APP_BUTTON);
        log.info("{} NemId code app request sent", NEM_ID_PREFIX);
    }

    private void displayPromptToOpenNemIdApp(Credentials credentials) {
        statusUpdater.updateStatusPayload(
                credentials, UserMessage.OPEN_NEM_ID_APP_AND_CLICK_BUTTON);

        Field field = DanishFields.NemIdInfo.build(catalog);

        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(Collections.singletonList(field)));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        supplementalRequester.requestSupplementalInformation(credentials, true);
    }
}
