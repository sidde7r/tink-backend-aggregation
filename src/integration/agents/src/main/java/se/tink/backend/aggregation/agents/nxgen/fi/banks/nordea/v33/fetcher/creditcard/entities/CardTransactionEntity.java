package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardTransactionEntity {
    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("booked")
    private boolean booked;

    @JsonProperty private double amount;
    @JsonProperty private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("booking_date")
    private Date bookingDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("transaction_date")
    private Date transactionDate;

    @JsonProperty("interest_date")
    private String interestDate;

    @JsonProperty("title")
    private String title;

    @JsonProperty("balance_after")
    private double balanceAfter;

    @JsonProperty("transaction_type")
    private String transactionType;

    public CreditCardTransaction toTinkCreditCardTransaction() {

        return CreditCardTransaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, NordeaFIConstants.CURRENCY))
                .setPending(!booked)
                .setDescription(String.format("%s", title))
                .setDate(getDate())
                .build();
    }

    private Date getDate() {
        return booked ? bookingDate : transactionDate;
    }
}
