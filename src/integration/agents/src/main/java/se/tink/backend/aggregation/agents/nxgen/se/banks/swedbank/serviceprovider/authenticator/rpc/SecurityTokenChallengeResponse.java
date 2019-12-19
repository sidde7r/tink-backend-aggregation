package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityTokenChallengeResponse {
    private String authenticationRole;
    private String easyLoginId;
    private String formattedServerTime;
    private LinksEntity links;
    private String maskedUserId;
    private String serverTime;

    public LinksEntity getLinks() {
        return Optional.ofNullable(links).orElseThrow(IllegalStateException::new);
    }
}
