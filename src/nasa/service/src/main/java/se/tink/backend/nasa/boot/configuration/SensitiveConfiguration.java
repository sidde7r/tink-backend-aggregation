package se.tink.backend.nasa.boot.configuration;

import java.util.Map;

public class SensitiveConfiguration {
    private String keyStorePassword;
    private String trustStorePassword;

    public SensitiveConfiguration() {
        Map<String, String> env = System.getenv();
        keyStorePassword = env.getOrDefault("KEYSTORE_PASSWORD", "changeme");
        trustStorePassword = env.getOrDefault("TRUSTSTORE_PASSWORD", "changeme");
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }
}
