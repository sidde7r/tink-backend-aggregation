package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard.rpc;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenFICreditCard;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenFICreditCardTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

public class CreditCardFITransactionsResponse extends CreditCardTransactionsResponse<HandelsbankenFICreditCard> {
    private List<HandelsbankenFICreditCardTransaction> transactions;

    @Override
    public List<CreditCardTransaction> tinkTransactions(HandelsbankenFICreditCard creditcard,
            CreditCardAccount account) {
        return transactions.stream()
                .map(handelsbankenFICreditCardTransaction -> handelsbankenFICreditCardTransaction
                        .toTinkTransaction(account))
                .collect(Collectors.toList());
    }

    @Override
    public List<CreditCardTransaction> tinkTransactions(CreditCardAccount account) {
        return transactions.stream()
                .map(handelsbankenFICreditCardTransaction -> handelsbankenFICreditCardTransaction
                        .toTinkTransaction(account))
                .collect(Collectors.toList());
    }
}
