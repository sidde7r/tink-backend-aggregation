package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCard;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    @JsonProperty("transaction_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    @JsonProperty("masked_credit_card_number")
    private String maskedCreditCardNumber;

    @JsonProperty("booking_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    @JsonProperty("transaction_type")
    private String transactionType;

    private double amount;

    private String beneficiary;

    private String currency;

    @JsonIgnore
    public ExactCurrencyAmount getTinkAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }

    public CreditCardTransaction toTinkCreditCardTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(getTinkAmount())
                .setCreditCard(CreditCard.create(beneficiary, maskedCreditCardNumber))
                .setDescription(beneficiary)
                .setPending(false)
                .setDate(transactionDate)
                .build();
    }
}
