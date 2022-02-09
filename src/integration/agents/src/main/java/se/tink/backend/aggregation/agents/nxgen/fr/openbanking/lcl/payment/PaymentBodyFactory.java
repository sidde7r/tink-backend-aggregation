package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.ConfirmPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.PaymentRequestResource;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PaymentBodyFactory {

    public PaymentRequestResource createPaymentRequestResource(CreatePaymentRequest request) {
        return new PaymentRequestResource(request);
    }

    public String serializeBody(PaymentRequestResource paymentRequestResource) {
        return SerializationUtils.serializeToString(paymentRequestResource);
    }

    public ConfirmPaymentRequest createConfirmPaymentRequest() {
        return new ConfirmPaymentRequest();
    }
}
