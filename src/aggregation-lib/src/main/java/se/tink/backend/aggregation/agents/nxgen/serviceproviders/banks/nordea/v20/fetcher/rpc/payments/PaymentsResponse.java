package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.rpc.payments;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.Payment;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.entities.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.entities.payments.PaymentList;

public abstract class PaymentsResponse {
    public abstract PaymentList getPaymentList();

    @JsonIgnore
    public abstract List<PaymentEntity> getPayments();

    @JsonIgnore
    public List<PaymentEntity> getPayments(Payment.StatusCode statusCode) {
        PaymentList paymentList = getPaymentList();
        if (paymentList == null) {
            return Lists.newArrayList();
        }

        return paymentList.getPayments(statusCode);
    }
}
