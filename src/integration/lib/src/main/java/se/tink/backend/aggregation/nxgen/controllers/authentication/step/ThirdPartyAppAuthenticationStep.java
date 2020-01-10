package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

public class ThirdPartyAppAuthenticationStep implements AuthenticationStep {

    private ThirdPartyAppAuthenticationPayload payload;
    private SupplementalWaitRequest supplementalWaitRequest;
    private CallbackProcessorMultiData callbackProcessor;

    public ThirdPartyAppAuthenticationStep(
            ThirdPartyAppAuthenticationPayload payload,
            SupplementalWaitRequest supplementalWaitRequest,
            CallbackProcessorMultiData callbackProcessor) {
        this.payload = payload;
        this.supplementalWaitRequest = supplementalWaitRequest;
        this.callbackProcessor = callbackProcessor;
    }

    @Override
    public Optional<SupplementInformationRequester> execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        if (request.getCallbackData() == null || request.getCallbackData().isEmpty()) {
            return Optional.of(
                    new SupplementInformationRequester.Builder()
                            .withThirdPartyAppAuthenticationPayload(payload)
                            .withSupplementalWaitRequest(supplementalWaitRequest)
                            .build());
        }
        callbackProcessor.process(request.getCallbackData());
        return Optional.empty();
    }
}
