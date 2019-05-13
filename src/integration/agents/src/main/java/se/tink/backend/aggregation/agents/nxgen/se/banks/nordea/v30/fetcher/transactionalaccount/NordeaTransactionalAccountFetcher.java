package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.EInvoiceEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class NordeaTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                UpcomingTransactionFetcher<TransactionalAccount> {
    private final NordeaSEApiClient apiClient;

    public NordeaTransactionalAccountFetcher(NordeaSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccount().toTinkAccount();
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        return apiClient.fetchEInvoice().getEInvoices().stream()
                .filter(EInvoiceEntity::isUnconfirmed)
                .filter(EInvoiceEntity::isPayment)
                .filter(
                        eInvoiceEntity ->
                                eInvoiceEntity.getFrom().equals(account.getAccountNumber()))
                .map(EInvoiceEntity::toUpcomingTransaction)
                .collect(Collectors.toList());
    }
}
