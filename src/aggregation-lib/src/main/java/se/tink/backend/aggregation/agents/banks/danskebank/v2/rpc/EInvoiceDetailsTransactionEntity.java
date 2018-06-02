package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.banks.danskebank.DanskeUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EInvoiceDetailsTransactionEntity {
    @JsonProperty("Amount")
    private Double amount;
    @JsonProperty("Receiver")
    private String receiver;
    @JsonProperty("Reference")
    private String reference;
    @JsonProperty("Text")
    private String text;
    @JsonProperty("Time")
    private String time;
    @JsonProperty("ToAccountId")
    private String toAccountId;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    @JsonIgnore
    public Date getDateFromTime() {
        return DanskeUtils.parseDanskeDate(getTime());
    }
}
