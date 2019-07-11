package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.entity.PsuDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UnicreditUserData {

    private PsuDataEntity psuData;

    public UnicreditUserData(String password) {
        this.psuData = new PsuDataEntity(password);
    }
}
