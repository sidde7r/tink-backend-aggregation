package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class LoggedInEntity {
    private String lastUsed;
    private String username;
    private String bankReference;
    private String scaToken;
}
