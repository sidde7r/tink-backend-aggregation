package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class Links {

    private Href scaStatus;
    private Href startAuthorisationWithEncryptedPsuAuthentication;
    private Href authoriseTransaction;
}
