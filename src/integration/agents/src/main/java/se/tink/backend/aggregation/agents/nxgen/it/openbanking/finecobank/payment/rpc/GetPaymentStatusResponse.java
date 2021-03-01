package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GetPaymentStatusResponse {
    private String transactionStatus;
}
