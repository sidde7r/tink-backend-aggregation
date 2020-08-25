package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserAssetsResponse {
    @JsonProperty("group_header")
    private GroupHeaderEntity groupHeader;

    @JsonProperty("response")
    private UserAssets userAssets;

    public String[] getScopes() {
        return userAssets.getScopes();
    }
}
