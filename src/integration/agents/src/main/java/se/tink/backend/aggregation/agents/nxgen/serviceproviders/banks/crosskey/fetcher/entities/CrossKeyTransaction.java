package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class CrossKeyTransaction {
    private double amount;
    private String currency;
    @JsonFormat(pattern = "yyyyMMdd")
    private Date dueDate;
    @JsonFormat(pattern = "yyyyMMdd")
    private Date bookingDate;
    private boolean incoming; //unverified... Not seen coming from Finnish Ålandsbanken backend
    private String ownNote; //unverified... Not seen coming from Finnish Ålandsbanken backend
    @JsonProperty("recieverName") // Typo in their api
    private String receiverName;
    private String textCode;
    private String transactionId;

    public Transaction toTinkTransaction(CrossKeyConfiguration agentConfiguration) {
        return agentConfiguration.parseTinkTransaction(this);
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public String getOwnNote() {
        return ownNote;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getTextCode() {
        return textCode;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
