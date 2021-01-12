package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.AuthenticationErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.CheckLoginResultResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.InitializeLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper.FortisOTPCalculator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RequiredArgsConstructor
public class AutoAuthStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final AgentPlatformFortisApiClient apiClient;
    private final FortisDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {

        FortisAuthDataAccessor authDataAccessor =
                dataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());

        FortisAuthData authData = authDataAccessor.get();

        final String maskedCardNumber = mask(authData.getUsername());
        final String cardFrameId = authData.getCardFrameId();
        final String smid = authData.getClientNumber();
        final String oathTokenId = authData.getOathTokenId();
        final String deviceId = authData.getDeviceId();

        InitializeLoginResponse initializeLoginResponse =
                apiClient.initializeLoginTransaction(
                        maskedCardNumber, cardFrameId, smid, oathTokenId, deviceId);

        if (initializeLoginResponse.isError()) {
            return new AgentFailedAuthenticationResult(
                    AuthenticationErrorHandler.getError(initializeLoginResponse, false),
                    request.getAuthenticationPersistedData());
        }

        String challenge = initializeLoginResponse.getValue().getEasyPin().getChallenge();

        String otp =
                FortisOTPCalculator.calculateOTP(
                        authData.getOcraKey(), challenge, System.currentTimeMillis() / 1000);

        CheckLoginResultResponse checkLoginResponse = apiClient.checkLoginResultEasyPin(smid, otp);

        if (checkLoginResponse.isError()) {
            return new AgentFailedAuthenticationResult(
                    AuthenticationErrorHandler.getError(checkLoginResponse, false),
                    request.getAuthenticationPersistedData());
        }

        apiClient.doEbewAppLogin(smid, FortisConstants.MeanIds.EAPI);

        return new AgentSucceededAuthenticationResult(request.getAuthenticationPersistedData());
    }

    private String mask(String cardNumber) {
        StringBuilder cardString = new StringBuilder(cardNumber);
        for (int index = 6; index < 13; index++) {
            cardString.setCharAt(index, 'X');
        }
        return String.valueOf(cardString);
    }
}
