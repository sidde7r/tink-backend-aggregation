package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

@AllArgsConstructor
@Slf4j
public class AccountFetchingStep implements AuthenticationStep {
    private final CbiGlobeApiClient apiClient;
    private final CbiUserState userState;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        GetAccountsResponse accountsResponse = apiClient.getAccounts();
        if (accountsResponse.getAccounts().isEmpty()) {
            log.info("No point to go further w/o accounts, authentication finished");
            return AuthenticationStepResponse.authenticationSucceeded();
        }
        userState.persistAccounts(accountsResponse);
        return AuthenticationStepResponse.executeNextStep();
    }
}
