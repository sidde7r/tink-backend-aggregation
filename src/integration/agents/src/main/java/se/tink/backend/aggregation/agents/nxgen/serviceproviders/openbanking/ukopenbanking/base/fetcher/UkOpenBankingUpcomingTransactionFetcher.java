package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class UkOpenBankingUpcomingTransactionFetcher<ResponseType>
        implements UpcomingTransactionFetcher<TransactionalAccount> {

    UkOpenBankingApiClient apiClient;
    Class<ResponseType> responseType;
    UpcomingTransactionConverter<ResponseType> converter;

    public UkOpenBankingUpcomingTransactionFetcher(
            UkOpenBankingApiClient apiClient,
            Class<ResponseType> responseType,
            UpcomingTransactionConverter<ResponseType> converter) {
        this.apiClient = apiClient;
        this.responseType = responseType;
        this.converter = converter;
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {

        return converter.toUpcomingTransactions(
                apiClient.fetchUpcomingTransactions(account.getBankIdentifier(), responseType));
    }
}
