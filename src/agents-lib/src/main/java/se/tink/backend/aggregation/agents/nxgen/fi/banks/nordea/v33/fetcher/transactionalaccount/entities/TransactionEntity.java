package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {
    @JsonProperty("transaction_id")
    private String transactionId;
    private boolean booked;
    private double amount;
    private String currency;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("booking_date")
    private Date bookingDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("transaction_date")
    private Date transactionDate;
    @JsonProperty("interest_date")
    private String interestDate;
    private String title;
    @JsonProperty("balance_after")
    private double balanceAfter;
    @JsonProperty("transaction_type")
    private TransactionTypeEntity transactionType;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(new Amount(NordeaFIConstants.CURRENCY, amount))
                .setPending(!booked)
                .setDescription(String.format("%s", title))
                .setDate(getDate())
                .setExternalId(transactionId)
                .build();
    }

    private Date getDate(){
        if(booked){
            return bookingDate;
        }
        return transactionDate;
    }
}
