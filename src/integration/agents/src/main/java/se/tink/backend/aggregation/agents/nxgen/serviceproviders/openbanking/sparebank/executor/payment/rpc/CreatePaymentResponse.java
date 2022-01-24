package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@Getter
@JsonObject
public class CreatePaymentResponse {

    private String paymentId;
    private String transactionStatus;
    private AmountEntity transactionFees;
    private boolean transactionFeeIndicator;
    private String psuMessage;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(
            CreatePaymentRequest createPaymentRequest,
            SparebankPaymentType paymentType,
            PaymentServiceType paymentServiceType,
            PaymentScheme paymentScheme) {
        Payment tinkPayment =
                new Payment.Builder()
                        .withUniqueId(paymentId)
                        .withStatus(
                                SparebankPaymentStatus.fromString(transactionStatus)
                                        .getTinkPaymentStatus())
                        .withType(paymentType.getTinkPaymentType())
                        .withPaymentServiceType(paymentServiceType)
                        .withPaymentScheme(paymentScheme)
                        .withCurrency(createPaymentRequest.getAmount().getCurrency())
                        .withExactCurrencyAmount(createPaymentRequest.getAmount().toTinkAmount())
                        .withCreditor(createPaymentRequest.getCreditorAccount().toTinkCreditor())
                        .build();

        return new PaymentResponse(tinkPayment);
    }
}
