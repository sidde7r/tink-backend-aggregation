package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardTransactionEntity {

    private static final int VALUE_DECIMAL_PLACES = 2;

    private Date bookDate;
    private String cardDescription;
    private BigDecimal creditAmount;
    private BigDecimal debitAmount;
    private String description;
    private Boolean fractionedPayment;
    private String maskedCardNumber;
    private String transactionId;
    private Date valueDate;

    public Transaction toTinkTransaction(String currency) {
        BigDecimal amount = creditAmount.subtract(debitAmount).movePointLeft(VALUE_DECIMAL_PLACES);

        return Transaction.builder()
                .setType(TransactionTypes.CREDIT_CARD)
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setDate(bookDate)
                .setDescription(description)
                .setRawDetails(cardDescription)
                .build();
    }
}
