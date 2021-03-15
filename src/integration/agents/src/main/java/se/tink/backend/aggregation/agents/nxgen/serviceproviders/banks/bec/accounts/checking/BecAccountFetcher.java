package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BecAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final BecApiClient apiClient;

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
                logger.info(
                        "tag={} Unknown type: {}",
                        BecConstants.Log.UNKOWN_ACCOUNT_TYPE,
                        details.getAccountType());
            }

            // Filter out non-transactional accounts
            if (!details.isTransactionalAccount()) {
                continue;
            }

            Optional<TransactionalAccount> transactionalAccount =
                    account.toTinkTransactionalAccount(details);
            transactionalAccount.ifPresent(transactionalAccounts::add);
        }

        return transactionalAccounts;
    }
}
