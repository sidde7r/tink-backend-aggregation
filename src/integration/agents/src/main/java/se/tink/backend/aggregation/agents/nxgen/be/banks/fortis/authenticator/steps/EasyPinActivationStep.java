package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.AuthenticationErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EasyPinActivateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper.FortisKeyDecryptor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper.FortisOTPCalculator;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RequiredArgsConstructor
public class EasyPinActivationStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final AgentPlatformFortisApiClient apiClient;
    private final FortisDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {

        FortisAuthDataAccessor authDataAccessor =
                dataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());

        FortisAuthData fortisAuthData = authDataAccessor.get();

        FortisProcessStateAccessor processStateAccessor =
                dataAccessorFactory.createProcessStateAccessor(
                        request.getAuthenticationProcessState());

        FortisProcessState processState = processStateAccessor.get();

        long timestamp = System.currentTimeMillis() / 1000;

        String key = EncodingUtils.encodeHexAsString(FortisKeyDecryptor.decryptKey(processState));
        String challenge = prepareChallenge(processState);
        String otp = FortisOTPCalculator.calculateOTP(key, challenge, timestamp);
        String gsn = processState.getLoginSessionId();
        String tokenId = processState.getOathTokenId();

        EasyPinActivateResponse easyPinActivateResponse =
                apiClient.easyPinActivate(otp, fortisAuthData.getClientNumber(), gsn, tokenId);

        if (easyPinActivateResponse.isError()) {
            return new AgentFailedAuthenticationResult(
                    AuthenticationErrorHandler.getError(easyPinActivateResponse),
                    request.getAuthenticationPersistedData());
        }

        fortisAuthData.setOcraKey(key);
        fortisAuthData.setDeviceId(processState.getDeviceId());
        fortisAuthData.setOathTokenId(processState.getOathTokenId());
        fortisAuthData.setCardFrameId(processState.getCardFrameId());

        return new AgentSucceededAuthenticationResult(authDataAccessor.store(fortisAuthData));
    }

    private String prepareChallenge(FortisProcessState processState) {
        return EncodingUtils.encodeHexAsString(
                processState.getEnrollmentSessionId().replace("-", "").getBytes());
    }
}
