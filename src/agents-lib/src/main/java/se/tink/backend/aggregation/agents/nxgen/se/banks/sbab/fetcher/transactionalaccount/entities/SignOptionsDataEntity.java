package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignOptionsDataEntity {
    @JsonProperty("sign_method")
    private String signMethod;

    @JsonProperty("return_url")
    private String returnUrl;

    public String getSignMethod() {
        return signMethod;
    }

    public String getReturnUrl() {
        return returnUrl;
    }
}
