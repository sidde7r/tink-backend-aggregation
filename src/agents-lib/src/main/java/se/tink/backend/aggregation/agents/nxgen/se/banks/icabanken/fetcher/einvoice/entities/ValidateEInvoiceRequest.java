package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidateEInvoiceRequest {
    @JsonProperty("DebitAccountId")
    private String accountId;
    @JsonProperty("InvoiceId")
    private String invoiceId;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }
}
