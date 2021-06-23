package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.src;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.entities.PaymentLinks;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.entities.RedsysTransactionStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@Getter
@JsonObject
public class PaymentInitiationResponse {
    private RedsysTransactionStatus transactionStatus;
    private String paymentId;
    private String psuMessage;
    private List<TppMessageEntity> tppMessages;

    @JsonProperty("_links")
    private PaymentLinks links;

    public RedsysTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public PaymentResponse toTinkPaymentResponse(PaymentRequest paymentRequest) {

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(paymentRequest.getPayment().getCreditor())
                        .withDebtor(paymentRequest.getPayment().getDebtor())
                        .withExactCurrencyAmount(
                                paymentRequest.getPayment().getExactCurrencyAmountFromField())
                        .withExecutionDate(paymentRequest.getPayment().getExecutionDate())
                        .withCurrency(paymentRequest.getPayment().getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(getTransactionStatus().getTinkStatus())
                        .withPaymentScheme(paymentRequest.getPayment().getPaymentScheme())
                        .withType(paymentRequest.getPayment().getType());
        return new PaymentResponse(buildingPaymentResponse.build(), paymentRequest.getStorage());
    }
}
