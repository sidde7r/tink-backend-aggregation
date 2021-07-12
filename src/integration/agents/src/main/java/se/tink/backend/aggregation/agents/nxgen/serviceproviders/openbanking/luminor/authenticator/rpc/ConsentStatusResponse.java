package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentStatusResponse {

    private String consentStatus;
}
