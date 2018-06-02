package se.tink.libraries.abnamro.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AbnAmroEnrollmentConfiguration {
    @JsonProperty
    private String url;

    @JsonProperty
    private String apiKey;

    public String getUrl() {
        return url;
    }

    public String getApiKey() {
        return apiKey;
    }
}
