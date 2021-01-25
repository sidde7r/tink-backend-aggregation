package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities.PaymentStatusEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class ConfirmPaymentResponse {

    private PaymentStatusEntity paymentRequest;
}
