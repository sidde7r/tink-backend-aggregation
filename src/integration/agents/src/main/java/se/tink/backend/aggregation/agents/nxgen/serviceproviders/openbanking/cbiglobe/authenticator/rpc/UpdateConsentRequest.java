package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@JsonObject
public class UpdateConsentRequest {
    private String authenticationMethodId;
}
