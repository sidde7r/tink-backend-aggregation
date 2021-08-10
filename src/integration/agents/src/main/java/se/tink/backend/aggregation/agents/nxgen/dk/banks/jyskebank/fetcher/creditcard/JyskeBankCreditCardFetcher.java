package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.creditcard;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@AllArgsConstructor
public class JyskeBankCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPagePaginator<CreditCardAccount> {
    private final JyskeBankApiClient apiClient;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        final AccountResponse accountResponse = apiClient.fetchAccounts();

        return ListUtils.emptyIfNull(accountResponse).stream()
                .filter(AccountsEntity::isCreditCardAccount)
                .map(AccountsEntity::toTinkCreditCardAccount)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        final TransactionResponse transactionResponse =
                apiClient.fetchTransactions(
                        account.getFromTemporaryStorage(Storage.PUBLIC_ID), page);

        return PaginatorResponseImpl.create(
                transactionResponse.toTinkTransactions(),
                transactionResponse.isHasMoreTransactions());
    }
}
