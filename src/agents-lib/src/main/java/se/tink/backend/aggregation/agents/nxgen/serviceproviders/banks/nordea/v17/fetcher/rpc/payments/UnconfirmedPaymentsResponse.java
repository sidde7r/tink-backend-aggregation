package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.rpc.payments;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants.Payment;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.entities.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.entities.payments.PaymentList;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UnconfirmedPaymentsResponse extends PaymentsResponse {
    @JsonProperty("getUnconfirmedPaymentsOut")
    private PaymentList paymentList;

    @Override
    public PaymentList getPaymentList() {
        return paymentList;
    }

    @Override
    public List<PaymentEntity> getPayments() {
        return getPayments(Payment.StatusCode.UNCONFIRMED);
    }
}
