package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.entity.KeyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmEnrollmentResponse {
    private String id;
    private KeyEntity key;
    private String nickname;
    private String state;
    private String type;

    @JsonProperty("valid_until")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date validUntil;

    public String getId() {
        return id;
    }

    public KeyEntity getKey() {
        return key;
    }

    public String getNickname() {
        return nickname;
    }

    public String getState() {
        return state;
    }

    public String getType() {
        return type;
    }

    public Date getValidUntil() {
        return validUntil;
    }
}
