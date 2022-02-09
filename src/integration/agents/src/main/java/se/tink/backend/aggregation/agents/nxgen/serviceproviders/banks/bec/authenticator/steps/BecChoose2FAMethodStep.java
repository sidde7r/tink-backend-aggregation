package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.Log.BEC_LOG_TAG;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdChoose2FAMethodField;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BecChoose2FAMethodStep {

    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public NemId2FAMethod choose2FAMethod(List<NemId2FAMethod> availableMethods) {
        if (availableMethods.isEmpty()) {
            throw NemIdError.SECOND_FACTOR_NOT_REGISTERED.exception();
        }

        if (availableMethods.size() == 1) {
            return availableMethods.get(0);
        }

        return askUserToChoose2FAMethod(availableMethods);
    }

    private NemId2FAMethod askUserToChoose2FAMethod(List<NemId2FAMethod> availableMethods) {
        List<NemId2FAMethod> sortedMethodsList = sortMethods(availableMethods);
        Field field = NemIdChoose2FAMethodField.build(catalog, sortedMethodsList);

        Map<String, String> supplementalInfoResponse =
                supplementalInformationController.askSupplementalInformationSync(field);

        String chosenMethodKey = supplementalInfoResponse.get(NemIdChoose2FAMethodField.FIELD_KEY);

        return NemId2FAMethod.getMethodBySupplementalInfoKey(chosenMethodKey, sortedMethodsList)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        String.format(
                                                "%s Cannot match any method for supplemental info response: %s",
                                                BEC_LOG_TAG, chosenMethodKey)));
    }

    private static List<NemId2FAMethod> sortMethods(List<NemId2FAMethod> nemId2FAMethods) {
        List<NemId2FAMethod> sortedMethodsList = new ArrayList<>(nemId2FAMethods);
        sortedMethodsList.sort(Comparator.comparing(NemId2FAMethod::getSupplementalInfoOrder));
        return sortedMethodsList;
    }
}
