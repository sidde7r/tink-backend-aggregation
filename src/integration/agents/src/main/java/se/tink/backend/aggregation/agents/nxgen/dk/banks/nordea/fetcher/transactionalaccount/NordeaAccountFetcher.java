package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class NordeaAccountFetcher
        implements AccountFetcher<TransactionalAccount>, TransactionFetcher<TransactionalAccount> {

    private final NordeaDkApiClient bankClient;

    public NordeaAccountFetcher(final NordeaDkApiClient bankClient) {
        this.bankClient = Objects.requireNonNull(bankClient);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return bankClient.getAccounts().getAccounts().stream()
                .filter(AccountEntity::isTransactionalAccount)
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        List<AggregationTransaction> result = new LinkedList<>();
        TransactionsResponse transactionsResponse;

        String continuationKey = null;
        do {
            transactionsResponse =
                    bankClient.getAccountTransactions(
                            account.getApiIdentifier(),
                            account.getFromTemporaryStorage(
                                    NordeaDkConstants.StorageKeys.PRODUCT_CODE),
                            continuationKey);
            result.addAll(
                    transactionsResponse.getTransactions().stream()
                            .map(TransactionEntity::toTinkTransaction)
                            .collect(Collectors.toList()));
            continuationKey = transactionsResponse.getContinuationKey();
        } while (continuationKey != null);

        return result;
    }
}
