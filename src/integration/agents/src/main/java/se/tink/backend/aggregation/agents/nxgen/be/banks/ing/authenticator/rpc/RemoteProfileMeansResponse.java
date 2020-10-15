package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class RemoteProfileMeansResponse {

    private String sid;
    private String salt;
    private String serverPublicValue;
    private String hmacSalt;
}
