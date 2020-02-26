package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.rpc.FetchCreditCardResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class CreditCardFetcher implements AccountFetcher<CreditCardAccount>, TransactionFetcher<CreditCardAccount> {
    private LansforsakringarApiClient apiClient;
    private static final AggregationLogger log = new AggregationLogger(CreditCardFetcher.class);

    public CreditCardFetcher(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        FetchCreditCardResponse fetchCreditCardResponse = apiClient.fetchCreditCards();
        log.info("FetchCardsListResponse:\n" + fetchCreditCardResponse.toString());

        return Collections.emptyList();
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return Lists.emptyList();
    }
}
