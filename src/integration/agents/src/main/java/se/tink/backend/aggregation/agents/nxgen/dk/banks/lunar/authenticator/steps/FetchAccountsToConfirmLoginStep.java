package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import agents_platform_framework.org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.client.AuthenticationApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception.LunarAuthExceptionHandler;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessState;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;

@RequiredArgsConstructor
@Slf4j
public class FetchAccountsToConfirmLoginStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final LunarDataAccessorFactory dataAccessorFactory;
    private final AuthenticationApiClient apiClient;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        LunarAuthDataAccessor authDataAccessor =
                dataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());
        LunarAuthData authData = authDataAccessor.get();

        LunarProcessState processState =
                dataAccessorFactory
                        .createProcessStateAccessor(request.getAuthenticationProcessState())
                        .get();

        String token = authData.getAccessToken();
        String deviceId = authData.getDeviceId();
        boolean isAutoAuth = processState.isAutoAuth();

        AccountsResponse accountsResponse;

        try {
            accountsResponse = apiClient.fetchAccounts(token, deviceId);
        } catch (ResponseStatusException e) {
            return LunarAuthExceptionHandler.getFetchAccountsFailedResult(
                    authDataAccessor, e, isAutoAuth);
        }

        if (accountsResponse == null) {
            log.error("Failed to fetch Lunar accounts!");
            return new AgentFailedAuthenticationResult(
                    getDefaultError(isAutoAuth), authDataAccessor.clearData());
        }

        authData.setAccountsResponse(accountsResponse);

        tryToFetchLoans(token, deviceId);

        return new AgentSucceededAuthenticationResult(authDataAccessor.storeData(authData));
    }

    private AgentBankApiError getDefaultError(boolean isAutoAuth) {
        if (isAutoAuth) {
            return new SessionExpiredError();
        }
        return new AuthorizationError();
    }

    private void tryToFetchLoans(String token, String deviceId) {
        // Wiski delete this or create loan fetcher after getting more data
        // Trying to get more information about loans since we don't have an ambassador
        try {
            if (!apiClient.fetchLoans(token, deviceId).getLoans().isEmpty()) {
                log.info("Found Lunar account with loans!");
            }
        } catch (RuntimeException e) {
            log.info("Failed to fetch Lunar loans!", e);
        }

        try {
            if (!apiClient.fetchCreditApplications(token, deviceId).getApplications().isEmpty()) {
                log.info("Found Lunar account with credit applications!");
            }
        } catch (RuntimeException e) {
            log.info("Failed to fetch Lunar credit applications!", e);
        }
    }
}
