package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BoursoramaGetPaymentResponse {

    private BoursoramaPaymentEntity paymentRequest;
}
