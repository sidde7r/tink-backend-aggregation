package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.BanquePopulaireApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
@Slf4j
public class BanquePopulaireTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final BanquePopulaireApiClient banquePopulaireApiClient;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {

        return banquePopulaireApiClient.fetchTransactionsForAccount(
                account.getApiIdentifier(), key);
    }
}
