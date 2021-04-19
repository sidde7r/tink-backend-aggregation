package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AllArgsConstructor
public class JyskeBankAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionPagePaginator<TransactionalAccount> {
    private final JyskeBankApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final AccountResponse accountResponse = apiClient.fetchAccounts();

        return accountResponse.stream()
                .map(AccountsEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        final TransactionResponse transactionResponse =
                apiClient.fetchTransactions(
                        account.getFromTemporaryStorage(Storage.PUBLIC_ID), page);

        return PaginatorResponseImpl.create(
                transactionResponse.toTinkTransactions(),
                transactionResponse.isHasMoreTransactions());
    }
}
