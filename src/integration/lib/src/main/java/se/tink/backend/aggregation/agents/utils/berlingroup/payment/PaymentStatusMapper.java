package se.tink.backend.aggregation.agents.utils.berlingroup.payment;

import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

public interface PaymentStatusMapper {
    PaymentStatus toTinkPaymentStatus(String bankStatus, PaymentServiceType paymentServiceType);
}
