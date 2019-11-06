package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public final class TransactionEntity {

    private static final int SCALE = 2;

    private String transactionId;

    private Date bookDate;

    private Date valueDate;

    private String transactionType;

    private long amount;

    private String description;

    private long bookBalanceAfterTransaction;

    private long availableBalanceAfterTransaction;

    public Transaction toTinkTransaction(String accountCurrency) {
        return Transaction.builder()
                .setDescription(description)
                .setDate(bookDate)
                .setAmount(
                        ExactCurrencyAmount.of(BigDecimal.valueOf(amount, SCALE), accountCurrency))
                .build();
    }
}
