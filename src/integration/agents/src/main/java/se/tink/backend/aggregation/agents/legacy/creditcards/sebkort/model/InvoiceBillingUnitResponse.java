package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceBillingUnitResponse extends BaseResponse {
    protected List<InvoiceEntity> body;

    public List<InvoiceEntity> getBody() {
        return body;
    }

    public void setBody(List<InvoiceEntity> body) {
        this.body = body;
    }
}
