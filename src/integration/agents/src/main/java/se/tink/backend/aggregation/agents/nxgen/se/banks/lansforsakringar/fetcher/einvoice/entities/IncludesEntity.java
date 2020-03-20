package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IncludesEntity {
    private String invoiceType;
    private List<String> eInvoiceStatuses;
    private String paymentType;

    @JsonIgnore
    public IncludesEntity(String invoiceType, List<String> eInvoiceStatuses, String paymentType) {
        this.invoiceType = invoiceType;
        this.eInvoiceStatuses = eInvoiceStatuses;
        this.paymentType = paymentType;
    }
}
