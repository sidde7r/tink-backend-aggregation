package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

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
