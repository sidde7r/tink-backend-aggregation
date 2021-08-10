package se.tink.backend.aggregation.agents.utils.berlingroup.payment;

import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.AspspPaymentStatus;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

public class BasePaymentStatusMapper implements PaymentStatusMapper {

    @Override
    public PaymentStatus toTinkPaymentStatus(
            String bankStatus, PaymentServiceType paymentServiceType) {
        return AspspPaymentStatus.fromString(bankStatus).getPaymentStatus();
    }
}
