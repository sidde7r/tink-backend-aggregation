package se.tink.backend.aggregation.configuration.integrations.abnamro;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AbnAmroEnrollmentConfiguration {
    @JsonProperty private String url;

    @JsonProperty private String apiKey;

    public String getUrl() {
        return url;
    }

    public String getApiKey() {
        return apiKey;
    }
}
