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
    private ThirdPartyAppRequestParamsProvider thirdPartyAppRequestParamsProvider;

    public ThirdPartyAppAuthenticationStep(
            ThirdPartyAppAuthenticationPayload payload,
            SupplementalWaitRequest supplementalWaitRequest,
            CallbackProcessorMultiData callbackProcessor) {
        this.payload = payload;
        this.supplementalWaitRequest = supplementalWaitRequest;
        this.callbackProcessor = callbackProcessor;
    }

    public ThirdPartyAppAuthenticationStep(
            ThirdPartyAppRequestParamsProvider thirdPartyAppRequestParamsProvider,
            CallbackProcessorMultiData callbackProcessor) {
        this.callbackProcessor = callbackProcessor;
        this.thirdPartyAppRequestParamsProvider = thirdPartyAppRequestParamsProvider;
    }

    @Override
    public Optional<SupplementInformationRequester> execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        if (request.getCallbackData() == null || request.getCallbackData().isEmpty()) {
            return Optional.of(
                    new SupplementInformationRequester.Builder()
                            .withThirdPartyAppAuthenticationPayload(getPayload())
                            .withSupplementalWaitRequest(getSupplementalWaitRequest())
                            .build());
        }
        callbackProcessor.process(request.getCallbackData());
        return Optional.empty();
    }

    public ThirdPartyAppAuthenticationPayload getPayload() {
        return payload != null ? payload : thirdPartyAppRequestParamsProvider.getPayload();
    }

    public SupplementalWaitRequest getSupplementalWaitRequest() {
        return supplementalWaitRequest != null
                ? supplementalWaitRequest
                : thirdPartyAppRequestParamsProvider.getWaitingConfiguration();
    }
}
