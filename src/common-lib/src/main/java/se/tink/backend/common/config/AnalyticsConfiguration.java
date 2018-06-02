package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnalyticsConfiguration {
    @JsonProperty
    private IntercomConfiguration intercom;

    public IntercomConfiguration getIntercom() {
        return intercom;
    }
}
