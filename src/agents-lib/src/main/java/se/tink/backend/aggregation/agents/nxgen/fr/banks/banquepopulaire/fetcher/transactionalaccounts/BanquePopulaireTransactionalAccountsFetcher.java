package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.AccountOverviewEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts.rpc.BanquePopulaireTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.rpc.AccountsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BanquePopulaireTransactionalAccountsFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionKeyPaginator<TransactionalAccount, String> {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(BanquePopulaireTransactionalAccountsFetcher.class);

    private final BanquePopulaireApiClient apiClient;

    public BanquePopulaireTransactionalAccountsFetcher(BanquePopulaireApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        AccountsResponse accountsResponse = apiClient.getAccounts();

        return accountsResponse.stream()
                .filter(account -> {
                    if (account.isUnknownType()) {
                        LOGGER.infoExtraLong(SerializationUtils.serializeToString(account),
                                BanquePopulaireConstants.LogTags.UNKNOWN_ACCOUNT_TYPE);
                        return false;
                    }

                    return true;
                })
                .map(AccountOverviewEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(TransactionalAccount account, String key) {
        return Optional.ofNullable(apiClient.getAccountTransactions(account, key))
                .orElse(BanquePopulaireTransactionsResponse.empty());
    }
}
