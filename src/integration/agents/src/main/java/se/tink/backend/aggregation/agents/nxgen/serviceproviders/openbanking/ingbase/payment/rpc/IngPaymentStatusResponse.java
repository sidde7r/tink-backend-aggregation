package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IngPaymentStatusResponse {

    private String transactionStatus;
}
