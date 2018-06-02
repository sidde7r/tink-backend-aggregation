package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppleKeystoreConfiguration {
    @JsonProperty
    private String password;
    @JsonProperty
    private String filename;

    public String getPassword() {
        return password;
    }

    public String getFilename() {
        return filename;
    }
}
