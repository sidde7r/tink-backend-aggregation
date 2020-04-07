package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import com.google.common.base.Strings;
import java.util.Collections;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.UbiConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class UbiUsernamePasswordAuthenticationStep implements AuthenticationStep {

    private final ConsentManager consentManager;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalRequester supplementalRequester;

    UbiUsernamePasswordAuthenticationStep(
            ConsentManager consentManager,
            StrongAuthenticationState strongAuthenticationState,
            SupplementalRequester supplementalRequester) {
        this.consentManager = consentManager;
        this.strongAuthenticationState = strongAuthenticationState;
        this.supplementalRequester = supplementalRequester;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        String username = request.getCredentials().getField(Field.Key.USERNAME);
        String password = request.getCredentials().getField(Key.PASSWORD);
        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        displayPrompt(request.getCredentials());

        processLogin(username, password);

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private void processLogin(final String username, final String password) throws LoginException {
        consentManager.createAccountConsent(strongAuthenticationState.getState());
        ConsentResponse accountConsentResponse =
                consentManager.updateAuthenticationMethod(FormValues.SCA_DECOUPLED);
        consentManager.updatePsuCredentials(
                username, password, accountConsentResponse.getPsuCredentials());

        consentManager.waitForAcceptance();

        consentManager.createTransactionsConsent(strongAuthenticationState.getState());
        ConsentResponse transactionsConsentResponse =
                consentManager.updateAuthenticationMethod(FormValues.SCA_DECOUPLED);
        consentManager.updatePsuCredentials(
                username, password, transactionsConsentResponse.getPsuCredentials());

        consentManager.waitForAcceptance();
    }

    private void displayPrompt(Credentials credentials) {
        Field field =
                Field.builder()
                        .immutable(true)
                        .description("Please open the bank application and confirm the order.")
                        .value("You will be asked for confirmation two times.")
                        .name("name")
                        .build();

        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(Collections.singletonList(field)));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);

        supplementalRequester.requestSupplementalInformation(credentials, false);
    }

    @Override
    public String getIdentifier() {
        return getStepIdentifier();
    }

    static String getStepIdentifier() {
        return UbiUsernamePasswordAuthenticationStep.class.getSimpleName();
    }
}
