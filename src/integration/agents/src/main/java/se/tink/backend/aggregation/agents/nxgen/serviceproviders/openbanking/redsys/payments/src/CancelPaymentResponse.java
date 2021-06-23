package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.src;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.entities.RedsysTransactionStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.payment.rpc.Payment;

@Getter
@JsonObject
public class CancelPaymentResponse {
    private RedsysTransactionStatus transactionStatus;
    private boolean fundsAvailable;
    private String psuMessage;
    private List<TppMessageEntity> tppMessages;

    public RedsysTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public PaymentResponse toTinkResponse() {
        Payment payment =
                new Payment.Builder().withStatus(getTransactionStatus().getTinkStatus()).build();
        return new PaymentResponse(payment, new TemporaryStorage());
    }
}
