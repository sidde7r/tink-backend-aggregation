package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinishEInvoiceSignRequest {
    @JsonProperty("RequestId")
    private String requestId;

    @JsonProperty("InvoiceId")
    private String invoiceId;

    private FinishEInvoiceSignRequest(String requestId, String invoiceId) {
        this.requestId = requestId;
        this.invoiceId = invoiceId;
    }

    public static FinishEInvoiceSignRequest create(String requestId, String invoiceId) {
        return new FinishEInvoiceSignRequest(requestId, invoiceId);
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getRequestId() {
        return requestId;
    }
}
