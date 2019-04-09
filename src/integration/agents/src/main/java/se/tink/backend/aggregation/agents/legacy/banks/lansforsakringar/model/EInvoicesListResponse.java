package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EInvoicesListResponse {

    private List<EInvoice> electronicInvoices;

    public List<EInvoice> getElectronicInvoices() {
        return electronicInvoices;
    }

    public void setElectronicInvoices(List<EInvoice> electronicInvoices) {
        this.electronicInvoices = electronicInvoices;
    }
}
