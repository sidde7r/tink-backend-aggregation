package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Date;
import se.tink.backend.aggregation.agents.banks.danskebank.DanskeUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EInvoiceListTransactionEntity {
    // Note that on the list entity, the Amount is negative. On the details entity it is positive.
    @JsonProperty("Amount")
    private Double amount;

    @JsonProperty("Invoice")
    private URI invoice;

    @JsonProperty("Receiver")
    private String receiver;

    @JsonProperty("Time")
    private String time;

    @JsonProperty("TransactionId")
    private String transactionId;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public URI getInvoice() {
        return invoice;
    }

    public void setInvoice(URI invoice) {
        this.invoice = invoice;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @JsonIgnore
    public Date getDateFromTime() {
        return DanskeUtils.parseDanskeDate(getTime());
    }

    @JsonIgnore
    public String getProviderUniqueID() {
        return getTransactionId();
    }
}
