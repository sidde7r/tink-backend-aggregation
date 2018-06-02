package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceDetailsResponse extends BaseResponse {
    protected InvoiceDetailsEntity body;

    public InvoiceDetailsEntity getBody() {
        return body;
    }

    public void setBody(InvoiceDetailsEntity body) {
        this.body = body;
    }
}
