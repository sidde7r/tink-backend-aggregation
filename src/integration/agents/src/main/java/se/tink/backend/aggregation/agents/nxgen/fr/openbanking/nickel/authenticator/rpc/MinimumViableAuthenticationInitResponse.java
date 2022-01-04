package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class MinimumViableAuthenticationInitResponse {

    private String challengeToken;
    private String challengeType;
    private String message;
    private String path;
    private Integer status;
}
