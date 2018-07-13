package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.fetchers.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LabelUserEntity {
    private String uuid;
    @JsonProperty("display_name")
    private String displayName;
    private String country;
    @JsonProperty("public_nick_name")
    private String publicNickName;

    public String getUuid() {
        return uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCountry() {
        return country;
    }

    public String getPublicNickName() {
        return publicNickName;
    }
}
