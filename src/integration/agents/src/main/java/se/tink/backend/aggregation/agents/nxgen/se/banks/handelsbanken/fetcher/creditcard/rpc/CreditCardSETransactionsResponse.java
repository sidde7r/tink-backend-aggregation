package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.rpc;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities.CardInvoiceInfo;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenSECreditCard;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenSECreditCardTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

public class CreditCardSETransactionsResponse
        extends CreditCardTransactionsResponse<HandelsbankenSECreditCard> {
    private CardInvoiceInfo cardInvoiceInfo;
    private List<HandelsbankenSECreditCardTransaction> transactions;

    public double findSpendable(HandelsbankenAmount fallback) {
        return findInCardInvoiceInfo(CardInvoiceInfo::findSpendable, fallback);
    }

    public double findUsedCredit(HandelsbankenAmount fallback) {
        Optional<HandelsbankenAmount> usedCredit = cardInvoiceInfo.findUsedCredit();

        // Try to use the usedCredit if it contains any value
        if (usedCredit.isPresent() && usedCredit.get().asDouble() != null) {
            return usedCredit.get().asDouble();
        }

        // Calculate balance based on credit limit and spendable amount if these values are present
        if (cardInvoiceInfo.hasCreditLimitLargerThanZero() && cardInvoiceInfo.hasSpendable()) {
            return cardInvoiceInfo
                    .getSpendable()
                    .asAmount()
                    .subtract(cardInvoiceInfo.getCredit().asAmount())
                    .doubleValue();
        }

        // Return fallback amount if it contains any value, otherwise default to 0
        return fallback != null ? fallback.asDouble() : 0;
    }

    private double findInCardInvoiceInfo(
            Function<CardInvoiceInfo, Optional<HandelsbankenAmount>> find,
            HandelsbankenAmount fallback) {
        HandelsbankenAmount amount =
                Optional.ofNullable(cardInvoiceInfo).flatMap(find).orElse(fallback);
        return amount != null ? amount.asDouble() : 0;
    }

    @Override
    public List<CreditCardTransaction> tinkTransactions(
            HandelsbankenSECreditCard creditcard, CreditCardAccount account) {
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
