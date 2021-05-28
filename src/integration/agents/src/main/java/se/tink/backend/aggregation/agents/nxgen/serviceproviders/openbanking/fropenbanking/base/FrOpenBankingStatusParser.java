package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;

public class FrOpenBankingStatusParser {

    public PaymentException parseErrorResponse(GetPaymentResponse paymentResponse) {
        return new PaymentRejectedException(
                "Unexpected payment status: " + paymentResponse.getPaymentStatus());
    }
}
