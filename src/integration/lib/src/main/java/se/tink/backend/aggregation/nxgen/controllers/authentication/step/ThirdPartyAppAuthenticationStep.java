package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class ThirdPartyAppAuthenticationStep extends AbstractAuthenticationStep {

    private ThirdPartyAppAuthenticationPayload payload;
    private SupplementalWaitRequest supplementalWaitRequest;
    private CallbackProcessorMultiData callbackProcessor;
    private ThirdPartyAppRequestParamsProvider thirdPartyAppRequestParamsProvider;

    public ThirdPartyAppAuthenticationStep(
            final String stepId,
            ThirdPartyAppAuthenticationPayload payload,
            SupplementalWaitRequest supplementalWaitRequest,
            CallbackProcessorMultiData callbackProcessor) {
        super(stepId);
        this.payload = payload;
        this.supplementalWaitRequest = supplementalWaitRequest;
        this.callbackProcessor = callbackProcessor;
    }

    public ThirdPartyAppAuthenticationStep(
            final String stepId,
            ThirdPartyAppRequestParamsProvider thirdPartyAppRequestParamsProvider,
            CallbackProcessorMultiData callbackProcessor) {
        super(stepId);
        this.callbackProcessor = callbackProcessor;
        this.thirdPartyAppRequestParamsProvider = thirdPartyAppRequestParamsProvider;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        if (request.getCallbackData() == null || request.getCallbackData().isEmpty()) {
            return AuthenticationStepResponse.requestForSupplementInformation(
                    new SupplementInformationRequester.Builder()
                            .withThirdPartyAppAuthenticationPayload(getPayload())
                            .withSupplementalWaitRequest(getSupplementalWaitRequest())
                            .build());
        }
        return callbackProcessor.process(request.getCallbackData());
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
