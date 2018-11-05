package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class OpenbankCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionFetcher<CreditCardAccount> {
    private final OpenbankApiClient apiClient;

    public OpenbankCreditCardFetcher(OpenbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient
                .getCards()
                .stream()
                .filter(CardEntity::isCreditCardAccount)
                .map(CardEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return Collections.emptyList();
    }
}
