package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class LaCaixaCreditCardFetcher implements AccountFetcher<CreditCardAccount>, TransactionFetcher<CreditCardAccount> {
    private static final AggregationLogger log = new AggregationLogger(LaCaixaApiClient.class);

    LaCaixaApiClient bankApi;

    public LaCaixaCreditCardFetcher(LaCaixaApiClient bankApi) {
        this.bankApi = bankApi;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {


        // TODO: Enable when we know how to properly filter credit/debit cards
        // return bankApi.fetchCreditCards().toTinkCards();

        String response = bankApi.fetchCreditCards();
        log.infoExtraLong(response, LaCaixaConstants.LogTags.CREDIT_CARDS);

        return Collections.emptyList();
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return Collections.emptyList();
    }
}
