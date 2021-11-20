package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc;

import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@Builder
public class AuthenticationsRequest {
    private String barcode;
    private String minimumViableToken;
    private String deviceToken;
    private String deviceType;
    private String version;
}
