package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.einvoice.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchPaymentsResponse {
    @JsonProperty("results")
    private List<PaymentEntity> payments;

    public List<PaymentEntity> getPayments() {
        return payments;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setPayments(List<PaymentEntity> payments) {
        this.payments = payments;
    }
}
