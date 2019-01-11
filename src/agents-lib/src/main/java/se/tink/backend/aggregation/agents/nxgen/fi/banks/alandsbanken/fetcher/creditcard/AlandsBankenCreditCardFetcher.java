package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.entities.AlandsBankenCard;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class AlandsBankenCreditCardFetcher implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(AlandsBankenCreditCardFetcher.class);

    private final AlandsBankenApiClient client;

    public AlandsBankenCreditCardFetcher(AlandsBankenApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        try {
            return this.client.fetchCards().stream().filter(AlandsBankenCard::isCreditCard)
                    .map(AlandsBankenCard::toCreditCardAccount)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.info("Error fetching credit cards: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {
        Collection<? extends Transaction> transactions = this.client.fetchCreditCardTransactions(
                account.getBankIdentifier(), fromDate, toDate).stream()
                .map(CreditCardTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());

        return PaginatorResponseImpl.create(transactions);
    }
}
