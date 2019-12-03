package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Encryptable;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdGenerateCodeRequest implements Encryptable {
    private boolean pushEnabled;

    public NemIdGenerateCodeRequest setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
        return this;
    }
}
