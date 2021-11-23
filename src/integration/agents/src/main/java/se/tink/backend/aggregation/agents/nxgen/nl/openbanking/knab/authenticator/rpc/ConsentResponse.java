package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@SuppressWarnings("UnusedDeclaration")
public class ConsentResponse {

    private String consentId;
}
