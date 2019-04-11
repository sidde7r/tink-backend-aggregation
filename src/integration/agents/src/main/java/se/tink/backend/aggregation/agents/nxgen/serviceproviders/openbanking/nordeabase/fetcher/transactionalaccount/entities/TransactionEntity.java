package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {

    @JsonProperty("_type")
    private String type;

    private Double amount;

    @JsonProperty("booking_date")
    private Date bookingDate;

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("counterparty_name")
    private String counterpartyName;

    private String currency;

    private String message;

    private String narrative;

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

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(getAmount())
                .setDate(transactionDate)
                .setDescription(typeDescription)
                .setPending(false)
                .build();
    }

    private Amount getAmount() {
        return new Amount(currency, amount);
    }
}
