package se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.executor;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
public final class ProgressiveLoginExecutor {

    private final SupplementalInformationController supplementalInformationController;
    private final ProgressiveAuthAgent agent;

    public ProgressiveLoginExecutor(
            final SupplementalInformationController supplementalInformationController,
            final ProgressiveAuthAgent agent) {
        this.supplementalInformationController = supplementalInformationController;
        this.agent = agent;
    }

    public void login(final CredentialsRequest credentialsRequest) throws Exception {
        SteppableAuthenticationResponse stepResponse =
                agent.login(
                        SteppableAuthenticationRequest.initialRequest(
                                credentialsRequest.getCredentials()));
        while (stepResponse.getStepIdentifier().isPresent()) {
            stepResponse =
                    agent.login(
                            handleSupplementInformationRequest(stepResponse, credentialsRequest));
        }
        credentialsRequest.getCredentials().setType(CredentialsTypes.PASSWORD);
    }

    private SteppableAuthenticationRequest handleSupplementInformationRequest(
            final SteppableAuthenticationResponse stepResponse,
            final CredentialsRequest credentialsRequest)
            throws Exception {
        if (!credentialsRequest.isManual()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        SupplementInformationRequester payload = stepResponse.getSupplementInformationRequester();
        checkIfPayloadNotEmpty(payload);
        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(credentialsRequest.getCredentials());
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
                            .orElseThrow(ThirdPartyAppError.TIMED_OUT::exception));
        }
        return Optional.empty();
    }

    private Optional<Map<String, String>> handleFieldsRequest(
            SupplementInformationRequester payload) throws SupplementalInfoException {
        if (payload.getFields().isPresent()) {
            final List<Field> fields = payload.getFields().get();
            log.info(
                    "Fields for which you are requesting: {}",
                    fields.stream().map(Field::getName).collect(Collectors.joining(",")));
            return Optional.of(
                    supplementalInformationController.askSupplementalInformationSync(
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
