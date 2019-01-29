package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities.CardBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.parsers.NordeaV21Parser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

public class NordeaV21CreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionKeyPaginator<CreditCardAccount, String> {
    private final NordeaV21ApiClient client;
    private final NordeaV21Parser parser;
    private final HashSet<String> fetchedTransactionKeys = Sets.newHashSet();

    public NordeaV21CreditCardFetcher(NordeaV21ApiClient client, NordeaV21Parser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return client.getAccountProductsOfTypes(NordeaV21Constants.ProductType.CARD).stream()
                .map(pe -> {
                    List<CardBalanceEntity> cardBalances = client.fetchCardBalances(pe.getNordeaAccountIdV2())
                            .getCardBalances();

                    // We ask for one credit card, we should only get one credit card in the response
                    Preconditions.checkState(cardBalances.size() == 1,
                            "%s: Received != 1 number of credit cards (%d)",
                            NordeaV21Constants.CREDIT_CARD_LOG_TAG,
                            cardBalances.size());

                    CardBalanceEntity cardBalance = cardBalances.get(0);
                    // Sometimes if the user isn't owner of the credit card it doesn't have access to the credit limit
                    if (cardBalance.getCreditLimit() == null) {
                        return null;
                    }

                    return parser.parseCreditCardAccount(pe, cardBalance);
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(CreditCardAccount account, String key) {
        if (!client.canViewTransactions(account)) {
            return new TransactionKeyPaginatorResponseImpl<>();
        }

        CreditCardTransactionsResponse transactionsResponse = client.fetchCreditCardTransactions(
                account.getBankIdentifier(), key);

        Collection<CreditCardTransaction> transactions = transactionsResponse.getTransactions().stream()
                .filter(te -> !fetchedTransactionKeys.contains(te.getTransactionKey()))
                .map(te -> {
                    CreditCardTransaction.Builder builder = parser.parseTransaction(te);
                    builder.setCreditAccount(account);
                    return builder.build();
                }).collect(Collectors.toList());

        this.fetchedTransactionKeys.addAll(transactionsResponse.getTransactions().stream()
                .map(CreditCardTransactionEntity::getTransactionKey)
                .collect(Collectors.toSet()));

        TransactionKeyPaginatorResponseImpl<String> response = new TransactionKeyPaginatorResponseImpl<>();
        response.setTransactions(transactions);
        response.setNext(transactionsResponse.getContinuationKey());

        return response;
    }
}
