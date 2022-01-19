package se.tink.backend.integration.tpp_secrets_service.client.configuration;

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
    private CertificatesLocation certificatesLocation;
    private boolean enabled;
    private String environment;
    private boolean enabledRetryPolicy;
    private boolean useSecretsServiceInternalClient;

    public boolean isEnabled() {
        return enabled;
    }

    public CertificatesLocation getCertificatesLocation() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(certificatesLocation.toString()),
                String.format(INVALID_CONFIGURATION, "certificatesLocation"));

        return certificatesLocation;
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

    public boolean isEnabledRetryPolicy() {
        return enabledRetryPolicy;
    }

    public boolean isUseSecretsServiceInternalClient() {
        return useSecretsServiceInternalClient;
    }
}
