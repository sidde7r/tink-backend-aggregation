package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.creditcard;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.rpc.Response;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class SebCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionFetcher<CreditCardAccount> {

    private final SebApiClient apiClient;
    private final SebSessionStorage sebSessionStorage;

    public SebCreditCardFetcher(
            final SebApiClient apiClient, final SebSessionStorage sebSessionStorage) {
        this.apiClient = Objects.requireNonNull(apiClient);
        this.sebSessionStorage = sebSessionStorage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        final Response response = apiClient.fetchCreditCardAccounts();
        final List<CreditCardEntity> creditCardEntities = response.getCreditCards();

        // Handle is a special value used to query for transactions for a specific account.
        // It is different in each session, so we have to save it in session storage while mapping.
        return creditCardEntities.stream()
                .map(
                        card -> {
                            CreditCardAccount res = card.toTinkAccount();
                            sebSessionStorage.putCardHandle(
                                    res.getIdModule().getUniqueId(), card.getHandle());
                            return res;
                        })
                .collect(Collectors.toList());
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {

        String handle = sebSessionStorage.getCardHandle(account.getIdModule().getUniqueId());

        final List<CreditCardTransactionEntity> pendingTransactions =
                apiClient
                        .fetchPendingCreditCardTransactions(handle)
                        .getPendingCreditCardTransactions();
        final List<CreditCardTransactionEntity> bookedTransactions =
                apiClient
                        .fetchBookedCreditCardTransactions(handle)
                        .getBookedCreditCardTransactions();

        List<AggregationTransaction> transactions =
                pendingTransactions.stream()
                        .map(t -> t.toTinkTransaction(account, true))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        transactions.addAll(
                bookedTransactions.stream()
                        .map(t -> t.toTinkTransaction(account, false))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()));

        return transactions;
    }
}
