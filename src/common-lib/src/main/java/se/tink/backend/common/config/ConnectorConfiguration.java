package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class ConnectorConfiguration {
    @JsonProperty
    private FlagsConfiguration flags;

    @JsonProperty
    private Map<String, List<String>> clients;

    @JsonProperty
    private String defaultProviderName;

    @JsonProperty
    private String webhookClientId;

    public FlagsConfiguration getFlags() { return flags; }

    public Map<String, List<String>> getClients() {
        return clients;
    }

    public String getDefaultProviderName() {
        return defaultProviderName;
    }

    public void setDefaultProviderName(String defaultProviderName) {
        this.defaultProviderName = defaultProviderName;
    }

    public String getWebhookClientId() {
        return webhookClientId;
    }
}
