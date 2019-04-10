package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionEntity {
    @JsonProperty("_type")
    private String type;

    private String transactionId;
    private String currency;
    private String bookingDate;
    private String valueDate;
    private String typeDescription;
    private String narrative;
    private String message;
    private String status;
    private String reference;
    private String counterpartyName;
    private String transactionDate;
    private String cardNumber;
    private String paymentDate;
    private String amount;
    private String ownMessage;
    private String originalCurrency;
    private String originalCurrencyAmount;
    private String currencyRate;

    public String getType() {
        return type;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public String getMessage() {
        return message;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public String getOwnMessage() {
        return ownMessage;
    }

    public String getNarrative() {
        return narrative;
    }

    public String getStatus() {
        return status;
    }

    public String getAmount() {
        return amount;
    }
}
