package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceBillingUnitsResponse extends BaseResponse {
    protected List<InvoiceBillingUnitEntity> body;

    public List<InvoiceBillingUnitEntity> getBody() {
        return body;
    }

    public void setBody(List<InvoiceBillingUnitEntity> body) {
        this.body = body;
    }
}
