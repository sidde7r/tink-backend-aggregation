package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class OpenBankingClientConfiguration implements ClientConfiguration {
    @JsonProperty private String redirectUrl;

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
