package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GetPaymentAuthStatusResponse {

    private static final String EXEMPTED = "exempted";
    private static final String FINALISED = "finalised";

    private String scaStatus;

    public boolean authFinishedSuccessfully() {
        return EXEMPTED.equalsIgnoreCase(scaStatus) || FINALISED.equalsIgnoreCase(scaStatus);
    }
}
