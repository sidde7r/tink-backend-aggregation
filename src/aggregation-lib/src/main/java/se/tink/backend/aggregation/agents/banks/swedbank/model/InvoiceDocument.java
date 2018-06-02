package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceDocument {

    private InvoiceDocumentData data;
    private String url;

    public InvoiceDocumentData getData() {
        return data;
    }

    public String getUrl() {
        return url;
    }

    public void setData(InvoiceDocumentData data) {
        this.data = data;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
