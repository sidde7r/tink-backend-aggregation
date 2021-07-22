package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment;

import java.util.EnumSet;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentStatusMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.AspspPaymentStatus;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

public class FiduciaPaymentStatusMapper implements PaymentStatusMapper {

    // Fiducia has weird behaviour.
    // For recurring payments, signed payment are still RCVD or PDNG.
    // This will overwrite only this status from the base mapper
    @Override
    public PaymentStatus toTinkPaymentStatus(
            String bankStatus, PaymentServiceType paymentServiceType) {
        AspspPaymentStatus aspspPaymentStatus = AspspPaymentStatus.fromString(bankStatus);
        if (paymentServiceType == PaymentServiceType.PERIODIC
                && EnumSet.of(AspspPaymentStatus.RECEIVED, AspspPaymentStatus.PENDING)
                        .contains(aspspPaymentStatus)) {
            return PaymentStatus.SIGNED;
        }

        return aspspPaymentStatus.getPaymentStatus();
    }
}
