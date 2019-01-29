package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.rpc;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenSECreditCard;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenSECreditCardTransaction;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities.CardInvoiceInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

public class CreditCardSETransactionsResponse extends CreditCardTransactionsResponse<HandelsbankenSECreditCard> {
    private CardInvoiceInfo cardInvoiceInfo;
    private List<HandelsbankenSECreditCardTransaction> transactions;

    public double findSpendable(HandelsbankenAmount fallback) {
        return findInCardInvoiceInfo(CardInvoiceInfo::findSpendable, fallback);
    }

    public double findUsedCredit(HandelsbankenAmount fallback) {
        return findInCardInvoiceInfo(CardInvoiceInfo::findUsedCredit, fallback);
    }

    private double findInCardInvoiceInfo(Function<CardInvoiceInfo, Optional<HandelsbankenAmount>> find,
            HandelsbankenAmount fallback) {
        HandelsbankenAmount amount = Optional.ofNullable(cardInvoiceInfo).flatMap(find).orElse(fallback);
        return amount != null ? amount.asDouble() : 0;
    }

    @Override
    public List<CreditCardTransaction> tinkTransactions(HandelsbankenSECreditCard creditcard,
            CreditCardAccount account) {
        return transactions.stream()
                .map(transaction -> transaction.toTinkTransaction(creditcard, account))
                .collect(Collectors.toList());
    }

    @Override
    public List<CreditCardTransaction> tinkTransactions(CreditCardAccount account) {
        return transactions.stream()
                .map(transaction -> transaction.toTinkTransaction(account))
                .collect(Collectors.toList());
    }
}
