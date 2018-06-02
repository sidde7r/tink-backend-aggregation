package se.tink.backend.rpc;

import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.auth.AuthenticationDetails;
import se.tink.api.headers.TinkHttpHeaders;

public class UserLogoutCommand {
    private boolean autologout;
    private Optional<String> userAgent = Optional.empty();
    private Optional<String> notificationToken = Optional.empty();
    private Optional<String> notificationPublicKey = Optional.empty();
    private Optional<String> deviceId = Optional.empty();
    private Optional<String> remoteAddress = Optional.empty();
    private Optional<AuthenticationDetails> authenticationDetails = Optional.empty();

    public UserLogoutCommand(boolean autologout, Map<String, String> headers) {
        this.autologout = autologout;
        if (headers != null) {
            this.userAgent = Optional.ofNullable(headers.get(HttpHeaders.USER_AGENT));
            this.notificationToken = Optional.ofNullable(headers.get(TinkHttpHeaders.NOTIFICATIONS_TOKEN_HEADER_NAME));
            this.notificationPublicKey = Optional
                    .ofNullable(headers.get(TinkHttpHeaders.NOTIFICATIONS_PUBLIC_KEY_HEADER_NAME));
            this.deviceId = Optional.ofNullable(headers.get(TinkHttpHeaders.DEVICE_ID_HEADER_NAME));
            this.remoteAddress = Optional.ofNullable(headers.get(TinkHttpHeaders.FORWARDED_FOR_HEADER_NAME));
            this.authenticationDetails = Optional.ofNullable(headers.get(HttpHeaders.AUTHORIZATION))
                    .map(AuthenticationDetails::new);
        }
    }

    public boolean isAutologout() {
        return autologout;
    }

    public Optional<String> getUserAgent() {
        return userAgent;
    }

    public Optional<String> getNotificationToken() {
        return notificationToken;
    }

    public Optional<String> getNotificationPublicKey() {
        return notificationPublicKey;
    }

    public Optional<String> getDeviceId() {
        return deviceId;
    }

    public Optional<String> getRemoteAddress() {
        return remoteAddress;
    }

    public Optional<AuthenticationDetails> getAuthenticationDetails() {
        return authenticationDetails;
    }
}
