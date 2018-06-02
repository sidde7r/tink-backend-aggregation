package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BackOfficeConfiguration {
    @JsonProperty
    private boolean enabled;
    @JsonProperty
    private String toAddress;
    @JsonProperty
    private String fromAddress;
    @JsonProperty
    private String fromName;
    @JsonProperty
    private String reportingEmailAddress;

    public boolean isEnabled() {
        return enabled;
    }

    public String getToAddress() {
        return toAddress;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getFromName() {
        return fromName;
    }

    public String getReportingEmailAddress() {
        return reportingEmailAddress;
    }
}
