package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserApiKeyEntity {

    @Getter private int id;
    private String created;
    private String updated;

    @JsonProperty("requested_by_user")
    private RequestedByUserEntity requestedByUser;

    @JsonProperty("granted_by_user")
    private GrantedByUserEntity grantedByUser;
}
