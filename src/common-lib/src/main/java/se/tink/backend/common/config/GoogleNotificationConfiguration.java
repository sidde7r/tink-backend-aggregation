package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import com.google.common.base.Strings;
import java.net.MalformedURLException;
import java.net.URL;

public class GoogleNotificationConfiguration {
    @JsonProperty
    private String apiKey;
    @JsonProperty
    private String proxyURL;

    public String getApiKey() {
        return apiKey;
    }

    public Optional<URL> getProxyURL() {
        if (Strings.isNullOrEmpty(proxyURL)) {
            return Optional.empty();
        }

        try {
            return Optional.of(new URL(proxyURL));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not parse notification proxy URL:"
                    + proxyURL, e);
        }
    }

    public void setProxyURL(String proxyURL) {
        this.proxyURL = proxyURL;
    }
}
