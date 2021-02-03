package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.SelectOption;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@RequiredArgsConstructor
public class UbiAuthenticationMethodChoiceStep implements AuthenticationStep {

    private static final LocalizableKey DESCRIPTION =
            new LocalizableKey("Do you have UBI Banca mobile app installed on this phone?");
    private static final String FIELD_NAME = "IS_APP_INSTALLED";
    private static final LocalizableKey HELPTEXT =
            new LocalizableKey(
                    "Please enter “Y” or “Yes“ if you have UBI Banca mobile app installed \n"
                            + "Please enter “N” or “No“ if you don’t have UBI Banca mobile app installed");
    private static final String PATTERN = "y|Y|yes|Yes|s|S|sì|Sì|SÌ|si|Si|SI|n|N|no|No|NO";
    private static final LocalizableKey PATTERN_ERROR_MSG =
            new LocalizableKey("The value you entered is not valid.");

    private static final LocalizableKey YES = new LocalizableKey("Yes");
    private static final LocalizableKey NO = new LocalizableKey("No");

    private final Catalog catalog;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        if (request.getUserInputs() == null || request.getUserInputs().isEmpty()) {
            return AuthenticationStepResponse.requestForSupplementInformation(
                    new SupplementInformationRequester.Builder()
                            .withFields(Collections.singletonList(buildMethodsField()))
                            .build());
        }

        String chosenMethod = request.getUserInputs().get(FIELD_NAME);
        if (shouldUseRedirectFlow(chosenMethod)) {
            return AuthenticationStepResponse.executeStepWithId(
                    CbiThirdPartyAppAuthenticationStep.getStepIdentifier(ConsentType.ACCOUNT));
        } else {
            return AuthenticationStepResponse.executeStepWithId(
                    AccountConsentDecoupledStep.getStepIdentifier());
        }
    }

    private Field buildMethodsField() {
        return Field.builder()
                .description(catalog.getString(DESCRIPTION))
                .helpText(catalog.getString(HELPTEXT))
                .name(FIELD_NAME)
                .minLength(1)
                .maxLength(3)
                .pattern(PATTERN)
                .patternError(catalog.getString(PATTERN_ERROR_MSG))
                .selectOptions(prepareSelectOptions())
                .build();
    }

    private List<SelectOption> prepareSelectOptions() {
        SelectOption selectOptionNoBankApp = new SelectOption(catalog.getString(NO), "n");
        SelectOption selectOptionBankApp = new SelectOption(catalog.getString(YES), "y");
        return Arrays.asList(selectOptionNoBankApp, selectOptionBankApp);
    }

    private boolean shouldUseRedirectFlow(String chosenMethod) {
        return chosenMethod.equalsIgnoreCase("n") || chosenMethod.equalsIgnoreCase("no");
    }
}
