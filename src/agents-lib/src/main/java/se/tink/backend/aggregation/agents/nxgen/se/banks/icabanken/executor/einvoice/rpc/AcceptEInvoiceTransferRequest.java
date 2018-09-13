package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AcceptEInvoiceTransferRequest {
    @JsonProperty("DebitAccountId")
    private String debitAccountId;
    @JsonProperty("InvoiceId")
    private String invoiceId;

    private AcceptEInvoiceTransferRequest(String accountId, String invoiceId) {
        this.debitAccountId = accountId;
        this.invoiceId = invoiceId;
    }

    public static AcceptEInvoiceTransferRequest create(String accountId, String invoiceId) {
        return new AcceptEInvoiceTransferRequest(accountId, invoiceId);
    }

    public String getDebitAccountId() {
        return debitAccountId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }
}
