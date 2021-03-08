package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
@EqualsAndHashCode
public class CreatePaymentRequest {
    private final AmountEntity instructedAmount;
    private final AccountEntity creditorAccount;
    private final String creditorName;
    private final String remittanceInformationUnstructured;
}
