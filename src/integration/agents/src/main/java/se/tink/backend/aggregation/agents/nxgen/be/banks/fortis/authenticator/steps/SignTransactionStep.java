package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.AuthenticationErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.ExecuteSignResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.InitiateSignResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

/**
 * This step is unused because server replied with unknown errors. In the end it was not needed to
 * perform authentication
 */
@RequiredArgsConstructor
public class SignTransactionStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final AgentPlatformFortisApiClient apiClient;
    private final FortisDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {

        FortisProcessState processState =
                dataAccessorFactory
                        .createProcessStateAccessor(request.getAuthenticationProcessState())
                        .get();

        FortisAuthDataAccessor authDataAccessor =
                dataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());

        FortisAuthData fortisAuthData = authDataAccessor.get();

        String tokenId = processState.getOathTokenId();

        InitiateSignResponse initiateSignResponse = apiClient.initiateSignTransaction(tokenId);

        if (initiateSignResponse.isError()) {
            return new AgentFailedAuthenticationResult(
                    AuthenticationErrorHandler.getError(initiateSignResponse),
                    request.getAuthenticationPersistedData());
        }

        initiateSignResponse.getBusinessMessageBulk().checkError();

        apiClient.retrieveSignMeans();
        ExecuteSignResponse executeSignResponse = apiClient.executeSignTransaction();

        if (executeSignResponse.isError()) {
            return new AgentFailedAuthenticationResult(
                    AuthenticationErrorHandler.getError(executeSignResponse),
                    request.getAuthenticationPersistedData());
        }

        return new AgentSucceededAuthenticationResult(authDataAccessor.store(fortisAuthData));
    }
}
