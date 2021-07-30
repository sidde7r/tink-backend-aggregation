package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants.Steps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.SwedbankBalticsAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

@RequiredArgsConstructor
public class InitSCAProcessStep implements AuthenticationStep {

    private final SwedbankBalticsAuthenticator authenticator;
    private final SwedbankBalticsApiClient apiClient;
    private final StepDataStorage stepDataStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        final Credentials credentials = request.getCredentials();

        final String userId =
                authenticator.verifyCredentialsNotNullOrEmpty(
                        credentials.getField(Field.Key.USERNAME));

        final String personalId =
                authenticator.verifyCredentialsNotNullOrEmpty(
                        credentials.getField(Key.NATIONAL_ID_NUMBER));

        AuthenticationResponse authenticationResponse =
                apiClient.authenticateDecoupledBaltics(userId, personalId);

        stepDataStorage.putAuthUrl(authenticationResponse.getCollectAuthUri());

        stepDataStorage.putChallengeCode(authenticationResponse.getChallengeData().getCode());

        // in case of smartID pop up comes automatically, no need to create additional
        // functionality for it

        return AuthenticationStepResponse.executeNextStep();
    }

    @Override
    public String getIdentifier() {
        return Steps.INIT_STEP;
    }
}
