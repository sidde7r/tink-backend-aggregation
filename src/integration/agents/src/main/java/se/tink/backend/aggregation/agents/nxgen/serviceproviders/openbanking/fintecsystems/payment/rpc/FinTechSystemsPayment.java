package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@NoArgsConstructor
@Getter
@Setter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class FinTechSystemsPayment {

    String id;
    String transaction;
    String senderHolder;
    String senderIban;
    String senderBic;
    String senderBankName;
    String senderCountryId;
    String recipientHolder;
    String recipientIban;
    String recipientBic;
    String recipientBankName;
    String recipientCountryId;
    String purpose;
    String amount;
    String currencyId;
    String paymentStatus;
    String testmode;
    String metadata;
    String merchantId;
    String createdAt;
    String object;

    @JsonIgnore
    public PaymentResponse toTinkPayment(Payment payment) {
        return new PaymentResponse(payment);
    }
}
