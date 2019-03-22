package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight;

import java.security.interfaces.RSAPublicKey;
import se.tink.backend.aggregation.nxgen.http.URL;

/** Static configuration for the Worklight client. */
public final class WLConfig {
    private final URL endpointUrl;
    private final RSAPublicKey publicKey;
    private final String moduleName;
    private final String appId;

    public WLConfig(
            final URL endpointUrl,
            final RSAPublicKey publicKey,
            final String moduleName,
            final String appId) {
        this.endpointUrl = endpointUrl;
        this.publicKey = publicKey;
        this.moduleName = moduleName;
        this.appId = appId;
    }

    public URL getEndpointUrl() {
        return endpointUrl;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getAppId() {
        return appId;
    }
}
