package se.tink.backend.integration.tpp_secrets_service.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TppSecretsServiceConfiguration {

    @JsonIgnore
    private static final String INVALID_CONFIGURATION =
            "Invalid Configuration: %s cannot be empty or null";

    private String host;
    private int port;
    private String caPath;
    private String tlsCrtPath;
    private String tlsKeyPath;
    private TppSecretsServiceClusterLocation tppSecretsServiceClusterLocation;

    public TppSecretsServiceClusterLocation getTppSecretsServiceClusterLocation() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(tppSecretsServiceClusterLocation.toString()),
                String.format(INVALID_CONFIGURATION, "tppSecretsServiceClusterLocation"));

        return tppSecretsServiceClusterLocation;
    }

    public String getCaPath() {
        return caPath;
    }

    public String getTlsCrtPath() {
        return tlsCrtPath;
    }

    public String getTlsKeyPath() {
        return tlsKeyPath;
    }

    public String getHost() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(host), String.format(INVALID_CONFIGURATION, "host"));
        return host;
    }

    public int getPort() {
        Preconditions.checkArgument(port != 0, String.format(INVALID_CONFIGURATION, "port"));
        return port;
    }
}
