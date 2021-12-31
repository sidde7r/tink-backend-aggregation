package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentDetailsResponse {

    private Access access;
    private String consentId;
}
