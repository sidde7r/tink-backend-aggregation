package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;

public class ServiceAuthenticationConfiguration {

    @JsonProperty("serverTokens")
    private List<String> serverTokens = Lists.newArrayList();
    @JsonProperty("yubikeys")
    private List<String> yubikeys = Lists.newArrayList();

    public List<String> getServerTokens() {
        return serverTokens;
    }

    public List<String> getYubikeys() {
        return yubikeys;
    }

}
