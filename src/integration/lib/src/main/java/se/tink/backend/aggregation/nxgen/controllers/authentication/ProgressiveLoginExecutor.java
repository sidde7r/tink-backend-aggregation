package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

public final class ProgressiveLoginExecutor {

    private final SupplementalInformationController supplementalInformationController;
    private final ProgressiveAuthAgent agent;

    public ProgressiveLoginExecutor(
            final SupplementalInformationController supplementalInformationController,
            final ProgressiveAuthAgent agent) {
        this.supplementalInformationController = supplementalInformationController;
        this.agent = agent;
    }

    public void login(final Credentials credentials) throws Exception {
        SteppableAuthenticationResponse stepResponse =
                agent.login(SteppableAuthenticationRequest.initialRequest(credentials));
        while (stepResponse.getStepIdentifier().isPresent()) {
            stepResponse =
                    agent.login(handleSupplementInformationRequest(stepResponse, credentials));
        }
    }

    private SteppableAuthenticationRequest handleSupplementInformationRequest(
            final SteppableAuthenticationResponse stepResponse, final Credentials credentials)
            throws Exception {
        SupplementInformationRequester payload = stepResponse.getSupplementInformationRequester();
        if (payload.getThirdPartyAppPayload().isPresent()) {
            supplementalInformationController.openThirdPartyApp(
                    payload.getThirdPartyAppPayload().get());

            return SteppableAuthenticationRequest.subsequentRequest(
                    stepResponse.getStepIdentifier().get(), new AuthenticationRequest(credentials));
        }

        if (payload.getSupplementalWaitRequest().isPresent()) {
            SupplementalWaitRequest waitRequest = payload.getSupplementalWaitRequest().get();

            final Map<String, String> callbackData =
                    supplementalInformationController
                            .waitForSupplementalInformation(
                                    waitRequest.getKey(),
                                    waitRequest.getWaitFor(),
                                    waitRequest.getTimeUnit())
                            .orElseThrow(
                                    LoginError.INCORRECT_CREDENTIALS
                                            ::exception); // todo: change this exception

            return SteppableAuthenticationRequest.subsequentRequest(
                    stepResponse.getStepIdentifier().get(),
                    new AuthenticationRequest(credentials).withCallbackData(callbackData));
        }

        if (payload.getFields().isPresent()) {
            final List<Field> fields = payload.getFields().get();
            final Map<String, String> map =
                    supplementalInformationController.askSupplementalInformation(
                            fields.toArray(new Field[fields.size()]));

            return SteppableAuthenticationRequest.subsequentRequest(
                    stepResponse.getStepIdentifier().get(),
                    new AuthenticationRequest(credentials).withUserInputs(map));
        }

        throw new IllegalStateException("The authentication response payload contained nothing");
    }
}
