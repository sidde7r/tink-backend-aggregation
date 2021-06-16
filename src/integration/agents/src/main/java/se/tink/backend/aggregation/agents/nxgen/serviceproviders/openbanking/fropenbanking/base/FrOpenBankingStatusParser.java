package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@Slf4j
public class FrOpenBankingStatusParser {

    public PaymentException parseErrorResponse(GetPaymentResponse paymentResponse) {
        Optional.ofNullable(paymentResponse.getStatusReasonInformation())
                .ifPresent(
                        reason -> log.info(String.format("StatusReasonInformation: %s", reason)));
        return new PaymentRejectedException(
                "Unexpected payment status: " + paymentResponse.getPaymentStatus(),
                InternalStatus.PAYMENT_REJECTED_BY_BANK_NO_DESCRIPTION);
    }
}
