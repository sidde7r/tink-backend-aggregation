package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity.CbiConsentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CbiConsentStatusResponse {

    private CbiConsentStatus consentStatus;
}
