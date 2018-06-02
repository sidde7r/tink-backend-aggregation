package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.CardInvoiceInfo;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.HandelsbankenSECreditCard;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.HandelsbankenSECreditCardTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
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
}
