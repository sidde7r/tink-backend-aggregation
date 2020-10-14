package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@RequiredArgsConstructor
public class NordeaCreditCardTransactionFetcher
        implements TransactionKeyPaginator<CreditCardAccount, Integer> {

    private final NordeaDkApiClient bankClient;

    @Override
    public TransactionKeyPaginatorResponse<Integer> getTransactionsFor(
            CreditCardAccount account, @Nullable Integer pageNumberKey) {
        return bankClient.fetchCreditCardTransactions(
                account.getApiIdentifier(), Optional.ofNullable(pageNumberKey).orElse(1));
    }
}
