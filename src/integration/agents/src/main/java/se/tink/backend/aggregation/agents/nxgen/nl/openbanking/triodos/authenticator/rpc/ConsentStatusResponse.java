package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentStatusResponse {

    @Getter private String consentStatus;
}
