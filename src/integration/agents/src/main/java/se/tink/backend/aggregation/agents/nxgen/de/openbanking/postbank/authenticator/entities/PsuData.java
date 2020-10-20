package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class PsuData {

    private String encryptedPassword;
}
