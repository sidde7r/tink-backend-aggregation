package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.creditcard.entities;

import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

@JsonObject
public class CreditCardTransactionEntity {
    private String merchantName;
    private String transactionType;
    private String transactionCurrency;
    private double totalAmount;
    private Date transactionDate;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(Amount.inEUR(totalAmount))
                .setDescription(createDescription())
                .setDate(transactionDate)
                .build();
    }

    private String createDescription() {
        return !Strings.isNullOrEmpty(merchantName) ? merchantName : transactionType;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }
}
