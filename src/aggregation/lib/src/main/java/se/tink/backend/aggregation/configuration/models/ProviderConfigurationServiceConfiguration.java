package se.tink.backend.aggregation.configuration.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProviderConfigurationServiceConfiguration {
    @JsonIgnore
    private static final String INVALID_CONFIGURATION =
            "Invalid Configuration: %s cannot be empty or null";

    private String host;
    private int port;
    private List<String> pinnedCertificates;
    private boolean enabled;

    public String getHost() {
        if (enabled) {
            Preconditions.checkNotNull(
                    Strings.emptyToNull(host), String.format(INVALID_CONFIGURATION, "host"));
        }
        return host;
    }

    public int getPort() {
        if (enabled) {
            Preconditions.checkArgument(port != 0, String.format(INVALID_CONFIGURATION, "port"));
        }
        return port;
    }

    public List<String> getPinnedCertificates() {
        return pinnedCertificates;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
