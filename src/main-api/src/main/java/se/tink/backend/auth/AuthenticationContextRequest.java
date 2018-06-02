package se.tink.backend.auth;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;

public class AuthenticationContextRequest {

    private AuthenticationDetails authenticationDetails;
    private String oauth2ClientId;
    private String clientKey;
    private String remoteAddress;
    private String userAgent;
    private String userDeviceId;
    private Map<String,String> headers;

    public Optional<AuthenticationDetails> getAuthenticationDetails() {
        return Optional.ofNullable(authenticationDetails);
    }

    public Optional<String> getClientKey() {
        return Optional.ofNullable(clientKey);
    }

    public Map<String, String> getHeaders() {
        if (headers == null) {
            return Maps.newHashMap();
        }
        return headers;
    }

    public Optional<String> getOauth2ClientId() {
        return Optional.ofNullable(oauth2ClientId);
    }

    public Optional<String> getRemoteAddress() {
        return Optional.ofNullable(remoteAddress);
    }

    public Optional<String> getUserAgent() {
        return Optional.ofNullable(userAgent);
    }

    public Optional<String> getUserDeviceId() {
        return Optional.ofNullable(userDeviceId);
    }

    public void setAuthenticationDetails(AuthenticationDetails authenticationDetails) {
        this.authenticationDetails = authenticationDetails;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public void setHeaders(Map<String,String> headers) {
        this.headers = headers;
    }

    public void setOauth2ClientId(String oauth2ClientId) {
        this.oauth2ClientId = oauth2ClientId;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setUserDeviceId(String userDeviceId) {
        this.userDeviceId = userDeviceId;
    }
}
