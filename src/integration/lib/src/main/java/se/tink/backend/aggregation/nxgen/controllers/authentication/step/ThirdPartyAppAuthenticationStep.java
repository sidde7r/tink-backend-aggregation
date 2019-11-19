package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

public class ThirdPartyAppAuthenticationStep<T> implements AuthenticationStep<T> {

    public interface CallbackProcessor<T> {
        void process(Map<String, String> callbackData, T persistentData)
                throws AuthenticationException, AuthorizationException;
    }

    private ThirdPartyAppAuthenticationPayload payload;
    private SupplementalWaitRequest supplementalWaitRequest;
    private CallbackProcessor callbackProcessor;

    public ThirdPartyAppAuthenticationStep(
            ThirdPartyAppAuthenticationPayload payload,
            SupplementalWaitRequest supplementalWaitRequest,
            CallbackProcessor callbackProcessor) {
        this.payload = payload;
        this.supplementalWaitRequest = supplementalWaitRequest;
        this.callbackProcessor = callbackProcessor;
    }

    @Override
    public Optional<SupplementInformationRequester> execute(
            AuthenticationRequest request, T persistentData)
            throws AuthenticationException, AuthorizationException {
        if (request.getCallbackData().isEmpty()) {
            return Optional.of(
                    new SupplementInformationRequester.Builder()
                            .withThirdPartyAppAuthenticationPayload(payload)
                            .withSupplementalWaitRequest(supplementalWaitRequest)
                            .build());
        }
        callbackProcessor.process(request.getCallbackData(), persistentData);
        return Optional.empty();
    }
}
