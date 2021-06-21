package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.fetcher.creditcard;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.NordeaBaseCreditCardFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class NordeaFiCreditCardFetcher
        extends NordeaBaseCreditCardFetcher<OneYearLimitCreditCardTransactionsResponse> {
    private final LocalDateTimeSource localDateTimeSource;

    public NordeaFiCreditCardFetcher(
            NordeaBaseApiClient apiClient,
            String currency,
            LocalDateTimeSource localDateTimeSource) {
        super(apiClient, currency, OneYearLimitCreditCardTransactionsResponse.class);
        this.localDateTimeSource = localDateTimeSource;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String key) {
        OneYearLimitCreditCardTransactionsResponse transactions =
                (OneYearLimitCreditCardTransactionsResponse) super.getTransactionsFor(account, key);
        transactions.setLocalDateTimeSource(localDateTimeSource);
        return transactions;
    }
}
