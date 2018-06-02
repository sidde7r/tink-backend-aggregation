package se.tink.backend.aggregationcontroller.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AggregationClusterConfiguration {
    @JsonProperty
    private boolean enabled;
    @JsonProperty
    private String host;
    @JsonProperty
    private String clientCertificateFilepath;

    public boolean isEnabled() {
        return enabled;
    }

    public String getHost() {
        return host;
    }

    public String getClientCertificateFilepath() {
        return clientCertificateFilepath;
    }

    public byte[] getClientCertificateBytes() {
        if (Strings.isNullOrEmpty(clientCertificateFilepath)) {
            return null;
        }

        Path path = Paths.get(clientCertificateFilepath);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
