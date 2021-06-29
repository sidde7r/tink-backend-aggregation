package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.src;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.entities.RedsysTransactionStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@Getter
@JsonObject
public class PaymentStatusResponse {
    private RedsysTransactionStatus transactionStatus;
    private String psuMessage;
    private List<TppMessageEntity> tppMessages;

    @JsonProperty("_links")
    private LinkEntity links;

    public void setTransactionStatus(RedsysTransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public RedsysTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public PaymentResponse toTinkPayment(Payment payment) {
        payment.setStatus(this.transactionStatus.getTinkStatus());
        return new PaymentResponse(payment);
    }
}
