package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TransactionEntity {

    private BigDecimal amount;

    private Date bookingDate;

    private String cardNumber;

    private String counterpartyAccount;

    private String counterpartyName;

    private String currency;

    private String currencyRate;

    private String message;

    private String narrative;

    private String originalCurrency;

    private String originalCurrencyAmount;

    private String ownMessage;

    private Date paymentDate;

    private String reference;

    private String status;

    private Date transactionDate;

    private String transactionId;

    private String typeDescription;

    private Date valueDate;

    public String getCounterpartyName() {
        return counterpartyName;
    }

    public String getNarrative() {
        return narrative;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(getAmount())
                .setDate(bookingDate)
                .setDescription(getDescription())
                .setPending(isPending())
                .build();
    }

    public String getDescription() {
        return (narrative != null) ? narrative : typeDescription;
    }

    private ExactCurrencyAmount getAmount() {
        return new ExactCurrencyAmount(amount, currency);
    }

    private boolean isPending() {
        return status.equalsIgnoreCase(NordeaBaseConstants.StatusResponse.RESERVED);
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public Date getValueDate() {
        return valueDate;
    }
}
