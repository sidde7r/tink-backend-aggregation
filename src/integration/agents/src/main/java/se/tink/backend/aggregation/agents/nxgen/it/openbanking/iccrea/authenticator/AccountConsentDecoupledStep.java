package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@AllArgsConstructor
public class AccountConsentDecoupledStep implements AuthenticationStep {
    private final ConsentManager consentManager;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;
    private final ConsentProcessor consentProcessor;

    private static final LocalizableKey DESCRIPTION =
            new LocalizableKey("Please open the bank application and confirm the order.");
    private static final String FIELD_NAME = "name";
    private static final LocalizableKey VALUE =
            new LocalizableKey("You will be asked for confirmation two times.");

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        String username = request.getCredentials().getField(Key.USERNAME);
        String password = request.getCredentials().getField(Key.PASSWORD);
        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        displayPrompt();

        ConsentScaResponse consentScaResponse =
                (ConsentScaResponse)
                        consentManager.createAccountConsent(strongAuthenticationState.getState());

        consentProcessor.processConsent(username, password, consentScaResponse);
        return AuthenticationStepResponse.executeNextStep();
    }

    private void displayPrompt() {
        Field field =
                CommonFields.Information.build(
                        FIELD_NAME, catalog.getString(DESCRIPTION), catalog.getString(VALUE), "");

        supplementalInformationController.askSupplementalInformationAsync(field);
    }
}
