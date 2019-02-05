package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities.CardsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.rpc.CardBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.parsers.NordeaV17Parser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants.ProductType;

public class NordeaV17CreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionKeyPaginator<CreditCardAccount, String> {
    private final NordeaV17ApiClient client;
    private final NordeaV17Parser parser;
    private final HashSet<String> fetchedTransactionKeys = Sets.newHashSet();

    public NordeaV17CreditCardFetcher(NordeaV17ApiClient client, NordeaV17Parser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return client.getAccountProductsOfTypes(ProductType.CARD).stream()
                .filter(ProductEntity::isCreditCard)
                .map(pe -> {
                    CardBalancesResponse cardBalancesResponse = client.fetchCardDetails(pe.getNordeaAccountIdV2());
                    List<CardsEntity> distinctCardEntities = cardBalancesResponse.getGetCardBalancesOut()
                            .getDistinctCardsList();

                    // We ask for one credit card, we should only get one distinct credit card in the response
                    Preconditions.checkState(distinctCardEntities.size() == 1,
                            "%s: Received != 1 number of credit cards (%d)",
                            NordeaV17Constants.CREDITCARD_LOG_TAG,
                            distinctCardEntities.size());

                    return parser.parseCreditCardAccount(pe, distinctCardEntities.get(0));
                })
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(CreditCardAccount account, String invoicePeriod) {
        if (!client.canViewTransactions(account)) {
            return new TransactionKeyPaginatorResponseImpl<>();
        }

        TransactionKeyPaginatorResponseImpl<String> paginatorResponse = new TransactionKeyPaginatorResponseImpl<>();

        CreditCardTransactionsResponse response = client.fetchCreditCardTransactions(
                account.getBankIdentifier(), invoicePeriod);

        Collection<CreditCardTransaction> transactions = response.getTransactions().stream()
                .filter(te -> !fetchedTransactionKeys.contains(te.getTransactionKey()))
                .map(te -> {
                    CreditCardTransaction.Builder builder = parser.parseTransaction(te);
                    builder.setCreditAccount(account);
                    return builder.build();
                }).collect(Collectors.toList());

        this.fetchedTransactionKeys.addAll(response.getTransactions().stream()
                .map(CreditCardTransactionEntity::getTransactionKey)
                .collect(Collectors.toSet()));

        paginatorResponse.setTransactions(transactions);
        Optional<String> nextInvoicePeriod = response.getNextInvoicePeriod();

        // There have been times when the Nordea servers deliver the same response over and over,
        // with the same continueKey. Throw Exception in order to avoid d-dosing the bank
        // TODO: Don't throw exception (if this error is frequent)
        nextInvoicePeriod.ifPresent(next -> Preconditions.checkState(!next.equalsIgnoreCase(invoicePeriod),
                String.format("current invoice-period (%s) == next invoice-period (%s)", invoicePeriod, nextInvoicePeriod)));

        paginatorResponse.setNext(nextInvoicePeriod.orElse(null));

        return paginatorResponse;
    }
}
