package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {

    private BigDecimal amount;

    @JsonProperty("booking_date")
    private Date bookingDate;

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("counterparty_account")
    private String counterPartyAccount;

    @JsonProperty("counterparty_name")
    private String counterPartyName;

    private String currency;

    @JsonProperty("currency_rate")
    private String currencyRate;

    private String message;

    private String narrative;

    @JsonProperty("original_currency")
    private String originalCurrency;

    @JsonProperty("original_currency_amount")
    private String originalCurrencyAmount;

    @JsonProperty("own_message")
    private String ownMessage;

    @JsonProperty("payment_date")
    private Date paymentDate;

    private String reference;

    private String status;

    @JsonProperty("transaction_date")
    private Date transactionDate;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("type_description")
    private String typeDescription;

    @JsonProperty("value_date")
    private Date valueDate;

    public String getCounterPartyName() {
        return counterPartyName;
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
