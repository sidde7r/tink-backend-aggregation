package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.ManualLoginEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ManualLoginRequest {
    private ManualLoginEntity data;

    private ManualLoginRequest(String userId, String pin) {
        this.data = ManualLoginEntity.create(userId, pin);
    }

    public static ManualLoginRequest create(String userId, String pin) {
        return new ManualLoginRequest(userId, pin);
    }
}
