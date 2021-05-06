package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.BaseTlcRequest.HeaderEntity;

@EqualsAndHashCode(callSuper = true)
public class UserIdHeaderEntity extends HeaderEntity {
    private static final String UID = "uid";

    @JsonProperty("uid")
    private final String userId;

    public UserIdHeaderEntity(String userId) {
        super(UID);
        this.userId = userId;
    }

    @Override
    public String getValue() {
        return userId;
    }
}
