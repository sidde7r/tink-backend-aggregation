package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ScaTokenEntity {
    private String scaToken;
    private boolean bindSca;
    private String scaInstanceId;

    public ScaTokenEntity(String scaToken) {
        bindSca = true;
        scaInstanceId = "";
        this.scaToken = scaToken;
    }
}
