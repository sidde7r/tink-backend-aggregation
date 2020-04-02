package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
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
        checkIfPayloadNotEmpty(payload);
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(credentials);
        handleThirdPartyAppPayload(payload);
        handleWaitRequest(payload)
                .ifPresent(callbackData -> authenticationRequest.withCallbackData(callbackData));
        handleFieldsRequest(payload)
                .ifPresent(userInputs -> authenticationRequest.withUserInputs(userInputs));
        return SteppableAuthenticationRequest.subsequentRequest(
                stepResponse.getStepIdentifier().get(), authenticationRequest);
    }

    private void handleThirdPartyAppPayload(SupplementInformationRequester payload) {
        if (payload.getThirdPartyAppPayload().isPresent()) {
            supplementalInformationController.openThirdPartyApp(
                    payload.getThirdPartyAppPayload().get());
        }
    }

    private Optional<Map<String, String>> handleWaitRequest(SupplementInformationRequester payload)
            throws LoginException {
        if (payload.getSupplementalWaitRequest().isPresent()) {
            SupplementalWaitRequest waitRequest = payload.getSupplementalWaitRequest().get();

            return Optional.of(
                    supplementalInformationController
                            .waitForSupplementalInformation(
                                    waitRequest.getKey(),
                                    waitRequest.getWaitFor(),
                                    waitRequest.getTimeUnit())
                            .orElseThrow(
                                    LoginError.INCORRECT_CREDENTIALS
                                            ::exception)); // todo: change this exception
        }
        return Optional.empty();
    }

    private Optional<Map<String, String>> handleFieldsRequest(
            SupplementInformationRequester payload) throws SupplementalInfoException {
        if (payload.getFields().isPresent()) {
            final List<Field> fields = payload.getFields().get();
            return Optional.of(
                    supplementalInformationController.askSupplementalInformation(
                            fields.toArray(new Field[fields.size()])));
        }
        return Optional.empty();
    }

    private void checkIfPayloadNotEmpty(SupplementInformationRequester payload) {
        Preconditions.checkState(
                payload.getFields().isPresent()
                        || payload.getSupplementalWaitRequest().isPresent()
                        || payload.getThirdPartyAppPayload().isPresent(),
                "The authentication response payload contained nothing");
    }
}
