package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.PaymentIdEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class BoursoramaCreditTransferTransactionEntity {

    private PaymentIdEntity paymentId;

    private BoursoramaAmountEntity instructedAmount;

    private BoursoramaRemittanceInformation remittanceInformation;
}
