package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionEntity {
    @JsonProperty("_type")
    private String type;
    @JsonProperty("transaction_id")
    private String transactionId;
    private String currency;
    @JsonProperty("booking_date")
    private String bookingDate;
    @JsonProperty("value_date")
    private String valueDate;
    @JsonProperty("type_description")
    private String typeDescription;
    private String narrative;
    private String message;
    private String status;
    private String reference;
    @JsonProperty("counterparty_name")
    private String counterpartyName;
    @JsonProperty("transaction_date")
    private String transactionDate;
    @JsonProperty("card_number")
    private String cardNumber;
    @JsonProperty("payment_date")
    private String paymentDate;
    private String amount;
    @JsonProperty("own_message")
    private String ownMessage;
    @JsonProperty("original_currency")
    private String originalCurrency;
    @JsonProperty("original_currency_amount")
    private String originalCurrencyAmount;
    @JsonProperty("currency_rate")
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
