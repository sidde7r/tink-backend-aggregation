package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.entities;

import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardTransactionDataEntity {
    private BigDecimal fee;
    private String establishment;
    private Date date;
    private BigDecimal amount;
    private String currency;
    private String transaction;
    private String transactionType;
    private String sequence;
    private boolean outstandingTransaction;
    private boolean postpone;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setDescription(establishment)
                .setDate(date)
                .setPending(outstandingTransaction)
                .build();
    }
}
