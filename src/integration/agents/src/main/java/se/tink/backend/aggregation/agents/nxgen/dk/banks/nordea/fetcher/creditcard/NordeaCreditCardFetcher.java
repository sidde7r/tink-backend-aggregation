package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class NordeaCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionFetcher<CreditCardAccount> {

    private final NordeaDkApiClient bankClient;

    public NordeaCreditCardFetcher(NordeaDkApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        CreditCardsResponse creditCardsResponse = bankClient.fetchCreditCards();
        final List<CreditCardAccount> result = new ArrayList<>();
        creditCardsResponse.getCards().stream()
                .filter(CreditCardEntity::isCreditCard)
                .forEach(
                        card -> {
                            result.add(
                                    bankClient
                                            .fetchCreditCardDetails(card.getCardId())
                                            .toTinkAccount());
                        });
        return result;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        CreditCardTransactionsResponse response;
        List<AggregationTransaction> transactions = new ArrayList<>();
        // TODO paging should be investigated in https://tinkab.atlassian.net/browse/ITE-1445
        int pageSize = 1;
        do {
            response = bankClient.fetchCreditCardTransactions(account.getApiIdentifier(), pageSize);
            response.getTransactions().forEach(t -> transactions.add(t.toTinkTransaction()));
            pageSize++;
        } while (response.getTransactions().size() != 0);

        return transactions;
    }
}
