package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AcceptEInvoiceTransferRequest {

    @JsonProperty("DebitAccountId")
    private String debitAccountId;
    @JsonProperty("InvoiceId")
    private String invoiceId;

    public String getDebitAccountId() {
        return debitAccountId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setDebitAccountId(String debitAccountId) {
        this.debitAccountId = debitAccountId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

}
