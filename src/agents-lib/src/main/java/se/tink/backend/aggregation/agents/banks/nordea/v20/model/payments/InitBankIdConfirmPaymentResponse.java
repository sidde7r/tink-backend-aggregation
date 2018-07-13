package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitBankIdConfirmPaymentResponse {

    @JsonProperty("initBankIdConfirmPaymentsOut")
    private InitBankIdConfirmPaymentsOut data;

    public InitBankIdConfirmPaymentsOut getData() {
        return data;
    }

    public void setData(InitBankIdConfirmPaymentsOut data) {
        this.data = data;
    }

    public Optional<String> getOrderRef() {
        String orderRef = data != null ? data.getOrderRef() : null;

        return Optional.ofNullable(orderRef);
    }
}
