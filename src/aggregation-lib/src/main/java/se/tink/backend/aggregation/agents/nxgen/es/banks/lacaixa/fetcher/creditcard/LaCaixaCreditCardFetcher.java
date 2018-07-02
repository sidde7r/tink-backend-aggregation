package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard;

import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class LaCaixaCreditCardFetcher implements AccountFetcher<CreditCardAccount>, TransactionFetcher<CreditCardAccount> {

    LaCaixaApiClient bankApi;

    public LaCaixaCreditCardFetcher(LaCaixaApiClient bankApi) {
        this.bankApi = bankApi;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return bankApi.fetchCreditCards().toTinkCards();
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return null;
    }
}
