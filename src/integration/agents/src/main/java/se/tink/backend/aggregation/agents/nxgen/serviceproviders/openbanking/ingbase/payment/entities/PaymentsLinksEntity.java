package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class PaymentsLinksEntity {
    private String scaRedirect;

    public String getAuthorizationUrl() {
        return scaRedirect;
    }
}
