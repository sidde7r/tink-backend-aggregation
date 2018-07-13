package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EndBankIdAuthenticationRequest {

    @JsonProperty("RequestId")
    private String requestId;
    @JsonProperty("InvoiceId")
    private String invoiceId;

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getRequestId() {
        return requestId;
    }

}
