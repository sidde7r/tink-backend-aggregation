package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConesntAuthorizationDataEntity {
    @JsonProperty("chosenScaApproach")
    private String chosenScaApproach;

    @JsonProperty("scaRedirectURL")
    private String scaRedirectURL;

    public String getScaRedirectURL() {
        return scaRedirectURL;
    }
}
