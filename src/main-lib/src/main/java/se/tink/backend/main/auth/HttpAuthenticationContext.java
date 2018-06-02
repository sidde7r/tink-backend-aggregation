package se.tink.backend.main.auth;

import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.auth.OAuth2ClientRequest;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.core.Client;
import se.tink.backend.core.User;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.api.headers.TinkHttpHeaders;

public class HttpAuthenticationContext implements AuthenticationContext {
    private User user;
    private HttpAuthenticationMethod httpAuthenticationMethod;
    private boolean administrativeMode;
    private String userAgent;
    private String remoteAddress;
    private String userDeviceId;
    private Optional<OAuth2Client> oAuth2Client = Optional.empty();
    private Optional<Client> client = Optional.empty();
    private Map<String, String> headers;

    public HttpAuthenticationContext(AuthenticatedUser authenticatedUser, HttpHeaders headers) {
        this.user = authenticatedUser.getUser();
        this.httpAuthenticationMethod = authenticatedUser.getMethod();
        this.administrativeMode = authenticatedUser.isAdministrativeMode();
        this.userAgent = RequestHeaderUtils.getUserAgent(headers);
        this.remoteAddress = RequestHeaderUtils.getRemoteIp(headers).orElse(null);
        this.userDeviceId = RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.DEVICE_ID_HEADER_NAME);
        this.headers = RequestHeaderUtils.getHeadersMap(headers);
    }

    public void setOAuth2ClientRequest(OAuth2ClientRequest oAuth2ClientRequest) {
        if (oAuth2ClientRequest == null) {
            return;
        }
        oAuth2Client = oAuth2ClientRequest.getoAuth2Client();
        client = oAuth2ClientRequest.getLinkClient();
    }

    @Override
    public Optional<OAuth2Client> getOAuth2Client() {
        return oAuth2Client;
    }

    @Override
    public Optional<Client> getClient() {
        return client;
    }

    @Override
    public Optional<String> getUserAgent() {
        return Optional.ofNullable(userAgent);
    }

    @Override
    public Optional<String> getRemoteAddress() {
        return Optional.ofNullable(remoteAddress);
    }

    @Override
    public Optional<String> getUserDeviceId() {
        return Optional.ofNullable(userDeviceId);
    }

    @Override
    public boolean isAuthenticated() {
        return user != null;
    }

    @Override
    public boolean isAdministrativeMode() {
        return administrativeMode;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public HttpAuthenticationMethod getHttpAuthenticationMethod() {
        return httpAuthenticationMethod;
    }

    @Override
    public Map<String, String> getMetadata() {
        return headers;
    }
}
