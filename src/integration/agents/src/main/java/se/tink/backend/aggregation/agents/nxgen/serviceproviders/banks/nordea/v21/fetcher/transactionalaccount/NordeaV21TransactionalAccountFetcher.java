package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.transactionalaccount;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.parsers.NordeaV21Parser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.agents.rpc.AccountTypes;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants.Payment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants.ProductType;

public class NordeaV21TransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>,
        UpcomingTransactionFetcher<TransactionalAccount>, TransactionKeyPaginator<TransactionalAccount, String> {
    private final NordeaV21ApiClient client;
    private final NordeaV21Parser parser;
    private final HashSet<String> fetchedTransactionKeys = Sets.newHashSet();

    public NordeaV21TransactionalAccountFetcher(NordeaV21ApiClient client, NordeaV21Parser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return client.getAccountProductsOfTypes(ProductType.ACCOUNT).stream()
                .filter(pe -> {
                    AccountTypes accountType = parser.getTinkAccountType(pe);
                    return TransactionalAccount.ALLOWED_ACCOUNT_TYPES.contains(accountType);
                }).map(parser::parseAccount)
                .collect(Collectors.toList());
    }

    @Override
    public List<UpcomingTransaction> fetchUpcomingTransactionsFor(TransactionalAccount account) {
        Optional<ProductEntity> productEntity = client.getAccountProductsOfTypes(ProductType.ACCOUNT).stream()
                .filter(ProductEntity::canView)
                .filter(pe -> Objects.equals(account.getBankIdentifier(), pe.getNordeaAccountIdV2()))
                .findFirst();

        if (!productEntity.isPresent()) {
            return Collections.emptyList();
        }

        return client.getPayments(productEntity.get(), Payment.StatusCode.CONFIRMED).stream()
                .map(parser::parseTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(TransactionalAccount account, String key) {
        if (!client.canViewTransactions(account)) {
            return new TransactionKeyPaginatorResponseImpl<>();
        }

        TransactionsResponse response = client.fetchTransactions(account.getBankIdentifier(), Strings.nullToEmpty(key));

        Collection<Transaction> transactions = response.getTransactions().stream()
                .filter(te -> !fetchedTransactionKeys.contains(te.getTransactionKey()))
                .map(parser::parseTransaction)
                .collect(Collectors.toList());

        this.fetchedTransactionKeys.addAll(response.getTransactions().stream()
                .map(TransactionEntity::getTransactionKey)
                .collect(Collectors.toSet()));

        TransactionKeyPaginatorResponseImpl<String> paginatorResponse = new TransactionKeyPaginatorResponseImpl<>();
        paginatorResponse.setTransactions(transactions);

        String nextKey = response.getContinueKey();

        if (!Strings.isNullOrEmpty(nextKey)) {
            // There have been times when the Nordea servers deliver the same response over and over,
            // with the same continueKey. Throw Exception in order to avoid d-dosing the bank
            // TODO: Don't throw exception (if this error is frequent)
            Preconditions.checkState(!nextKey.equalsIgnoreCase(key),
                    String.format("currentKey (%s) == nextKey (%s)", key, response.getContinueKey()));
        }

        paginatorResponse.setNext(Strings.emptyToNull(nextKey));

        return paginatorResponse;
    }
}
