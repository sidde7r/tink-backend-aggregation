package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoicesResponse {
    
    private List<Invoice> einvoices;

    public List<Invoice> getEinvoices() {
        return einvoices;
    }

    public void setEinvoices(List<Invoice> einvoices) {
        this.einvoices = einvoices;
    }
}
