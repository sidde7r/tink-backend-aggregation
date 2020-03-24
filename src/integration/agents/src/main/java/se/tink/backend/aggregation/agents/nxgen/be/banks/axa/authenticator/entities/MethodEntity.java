package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MethodEntity {

    private String assertionId;
    private List<ChannelEntity> channels;
    private Boolean expired;
    private String lastUsed;
    private Boolean locked;
    private OtpFormatEntity otpFormat;
    private Integer retriesLeft;
    private String state;
    private String status;
    private String type;

    public String getAssertionId() {
        return assertionId;
    }

    public List<ChannelEntity> getChannels() {
        return channels;
    }

    public Boolean getExpired() {
        return expired;
    }

    public String getLastUsed() {
        return lastUsed;
    }

    public Boolean getLocked() {
        return locked;
    }

    public OtpFormatEntity getOtpFormat() {
        return otpFormat;
    }

    public Integer getRetriesLeft() {
        return retriesLeft;
    }

    public String getState() {
        return state;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }
}
