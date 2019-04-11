package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.creditcard;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.creditcard.rpc.CreditCardDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class OmaspCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionFetcher<CreditCardAccount> {
    private final OmaspApiClient apiClient;

    public OmaspCreditCardFetcher(OmaspApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CreditCardEntity> cards = apiClient.getCreditCards();

        return cards.stream()
                .filter(CreditCardEntity::isCreditCard)
                .map(CreditCardEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        CreditCardDetailsResponse cardDetails =
                apiClient.getCreditCardDetails(account.getBankIdentifier());
        return cardDetails.getTransactions().stream()
                .map(transaction -> transaction.toTinkTransaction(account))
                .collect(Collectors.toList());
    }
}
