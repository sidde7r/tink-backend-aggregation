package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserRequesterGranter {
    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("public_nick_name")
    private String publicNickName;

    private AvatarEntity avatar;

    @JsonProperty("session_timeout")
    private int sessionTimeout;
}
