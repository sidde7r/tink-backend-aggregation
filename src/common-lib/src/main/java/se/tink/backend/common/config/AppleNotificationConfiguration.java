package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import com.google.common.base.Strings;
import java.net.MalformedURLException;
import java.net.URL;

public class AppleNotificationConfiguration {
    @JsonProperty
    AppleKeystoreConfiguration keystore;
    @JsonProperty
    private String proxyURL;
    @JsonProperty
    private boolean sandbox = false;
    @JsonProperty
    private boolean updateAppIconNotificationBadge = true;

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

    // Also known as "Bundle ID" in Apple documentation:
    // https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/ApplePushService.html
    @JsonProperty
    private String topic;

    public AppleKeystoreConfiguration getKeystore() {
        return keystore;
    }

    public String getTopic() {
        return topic;
    }

    public boolean isSandbox() {
        return sandbox;
    }

    public void setProxyURL(String proxyURL) {
        this.proxyURL = proxyURL;
    }

    public boolean isUpdateAppIconNotificationBadge() {
        return updateAppIconNotificationBadge;
    }

}
