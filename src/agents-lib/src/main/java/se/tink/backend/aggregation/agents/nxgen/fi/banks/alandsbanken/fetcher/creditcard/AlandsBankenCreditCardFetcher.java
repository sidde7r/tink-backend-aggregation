package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.creditcard;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.entities.AlandsBankenCard;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class AlandsBankenCreditCardFetcher implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {

    private final AlandsBankenApiClient client;

    public AlandsBankenCreditCardFetcher(AlandsBankenApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return this.client.fetchCards().stream().filter(AlandsBankenCard::isCreditCard)
                .map(AlandsBankenCard::toCreditCardAccount)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends Transaction> getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {
        return this.client.fetchCreditCardTransactions(account.getBankIdentifier(), fromDate, toDate).stream()
                .map(CreditCardTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
