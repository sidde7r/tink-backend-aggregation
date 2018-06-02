package se.tink.libraries.abnamro.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrustStoreConfiguration {
    @JsonProperty
    private String path;
    @JsonProperty
    private String password;

    public String getPath() {
        return path;
    }

    public String getPassword() {
        return password;
    }
}
