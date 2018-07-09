package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.fetcher.transactional;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.FidorApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities.OpenTokenEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class FidorAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final FidorApiClient fidorApiClient;

    public FidorAccountFetcher(FidorApiClient fidorApiClient){
        this.fidorApiClient = fidorApiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        OpenTokenEntity tokenEntity = fidorApiClient.getTokenFromStorage();
        return fidorApiClient.fetchOpenApiAccounts(tokenEntity).toTransactionalAccounts();
    }

}
