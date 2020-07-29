package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BecAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final BecApiClient apiClient;
    private static final AggregationLogger logger = new AggregationLogger(BecAccountFetcher.class);

    public BecAccountFetcher(BecApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<TransactionalAccount> transactionalAccounts = new ArrayList<>();
        FetchAccountResponse accountsResponse = apiClient.fetchAccounts();
        for (AccountEntity account : accountsResponse) {
            AccountDetailsResponse details = apiClient.fetchAccountDetails(account.getAccountId());

            if (details.isUnknownType()) {
                // log unknown type
                logger.infoExtraLong(
                        String.format("Unknown type: %s\n", details.getAccountType()),
                        BecConstants.Log.UNKOWN_ACCOUNT_TYPE);
            }

            // Filter out non-transactional accounts
            if (!details.isTransactionalAccount()) {
                continue;
            }

            TransactionalAccount transactionalAccount = account.toTinkTransactionalAccount(details);
            transactionalAccounts.add(transactionalAccount);
        }

        return transactionalAccounts;
    }
}
