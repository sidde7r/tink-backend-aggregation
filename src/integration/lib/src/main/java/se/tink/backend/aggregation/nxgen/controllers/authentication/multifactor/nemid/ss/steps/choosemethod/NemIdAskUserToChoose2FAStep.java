package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel.WAITING_FOR_SUPPLEMENTAL_INFO_METRIC;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage.CHOOSE_NEM_ID_METHOD;

import com.google.inject.Inject;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
class NemIdAskUserToChoose2FAStep {

    private final NemIdMetrics metrics;
    private final NemIdCredentialsStatusUpdater statusUpdater;
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public NemId2FAMethod askUserToChoose2FAMethod(
            Credentials credentials, Set<NemId2FAMethod> availableMethods) {
        return metrics.executeWithTimer(
                () -> askUserFor2FAMethod(credentials, availableMethods),
                WAITING_FOR_SUPPLEMENTAL_INFO_METRIC);
    }

    private NemId2FAMethod askUserFor2FAMethod(
            Credentials credentials, Set<NemId2FAMethod> availableMethods) {

        Field field = NemIdChoose2FAMethodField.build(catalog, availableMethods);

        statusUpdater.updateStatusPayload(credentials, CHOOSE_NEM_ID_METHOD);
        Map<String, String> supplementalInfoResponse =
                supplementalInformationController.askSupplementalInformationSync(field);

        String chosenMethodKey = supplementalInfoResponse.get(NemIdChoose2FAMethodField.FIELD_KEY);

        return NemId2FAMethod.getMethodBySupplementalInfoKey(chosenMethodKey)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Cannot match any method for supplemental info response: \""
                                                + chosenMethodKey
                                                + "\""));
    }
}
