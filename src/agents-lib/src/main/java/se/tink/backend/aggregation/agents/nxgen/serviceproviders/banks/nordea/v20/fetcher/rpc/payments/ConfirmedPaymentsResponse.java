package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.rpc.payments;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.Payment;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.entities.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.entities.payments.PaymentList;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmedPaymentsResponse extends PaymentsResponse {
    @JsonProperty("getPaymentListOut")
    private PaymentList paymentList;

    @Override
    public PaymentList getPaymentList() {
        return paymentList;
    }

    @Override
    public List<PaymentEntity> getPayments() {
        return getPayments(Payment.StatusCode.CONFIRMED);
    }
}
