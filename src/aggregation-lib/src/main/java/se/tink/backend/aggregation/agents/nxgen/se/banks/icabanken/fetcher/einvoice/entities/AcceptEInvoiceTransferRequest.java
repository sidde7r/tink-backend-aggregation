package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
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
