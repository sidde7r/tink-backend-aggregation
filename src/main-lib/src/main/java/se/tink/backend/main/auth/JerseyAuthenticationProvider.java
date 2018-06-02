package se.tink.backend.main.auth;

import com.google.api.client.util.Strings;
import com.google.inject.Inject;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationDetails;
import se.tink.backend.auth.AuthenticationRequirements;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.core.User;
import se.tink.backend.main.auth.authenticators.DefaultRequestAuthenticator;
import se.tink.backend.main.auth.exceptions.UnauthorizedDeviceException;
import se.tink.backend.main.auth.exceptions.UnsupportedClientException;
import se.tink.backend.main.auth.exceptions.jersey.UnauthorizedDeviceJerseyException;
import se.tink.backend.main.auth.exceptions.jersey.UnsupportedClientJerseyException;
import se.tink.backend.utils.LogUtils;
import se.tink.api.headers.TinkHttpHeaders;
import se.tink.libraries.auth.HttpAuthenticationMethod;

public class JerseyAuthenticationProvider implements InjectableProvider<Authenticated, Type> {
    private static final LogUtils log = new LogUtils(JerseyAuthenticationProvider.class);

    private final DefaultRequestAuthenticator requestAuthenticator;

    @Inject
    public JerseyAuthenticationProvider(final DefaultRequestAuthenticator requestAuthenticator) {
        this.requestAuthenticator = requestAuthenticator;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable getInjectable(ComponentContext componentContext, Authenticated authenticated, Type type) {
        if (type.equals(AuthenticationContext.class)) {
            return new RequestInjectable(authenticated);
        } else if (type.equals(AuthenticatedUser.class)) {
            return new AuthenticatedUserInjectable(authenticated);
        } else if (type.equals(User.class)) {
            return new UserInjectable(authenticated);
        }

        throw new IllegalArgumentException("No injector found!");
    }

    private DefaultAuthenticationContext authenticate(HttpContext httpContext, Authenticated authenticated) {

        AuthenticationContextRequest authenticationContextRequest = build(httpContext.getRequest());

        try {
            return requestAuthenticator.authenticate(AuthenticationRequirements.fromAuthenticated(authenticated),
                    authenticationContextRequest);
        } catch (UnsupportedClientException e) {
            throw new UnsupportedClientJerseyException(e.getMessage());
        } catch (UnauthorizedDeviceException e) {
            throw new UnauthorizedDeviceJerseyException(e.getMfaUrl());
        } catch (Throwable e) {
            log.warn("Failed to authenticate", e);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    private AuthenticationContextRequest build(HttpRequestContext httpRequestContext) {

        String authorizationHeader = httpRequestContext.getHeaderValue(HttpHeaders.AUTHORIZATION);
        String sessionIdHeader = httpRequestContext.getHeaderValue(TinkHttpHeaders.SESSION_ID_HEADER_NAME);
        String clientKey = httpRequestContext.getHeaderValue(TinkHttpHeaders.CLIENT_KEY_HEADER_NAME);
        String oauth2ClientId = httpRequestContext.getHeaderValue(TinkHttpHeaders.OAUTH_CLIENT_ID_HEADER_NAME);
        String deviceId = httpRequestContext.getHeaderValue(TinkHttpHeaders.DEVICE_ID_HEADER_NAME);
        String userAgent = RequestHeaderUtils.getUserAgent(httpRequestContext);
        Optional<String> remoteAddress = RequestHeaderUtils.getRemoteIp(httpRequestContext);

        AuthenticationDetails authenticationDetails = null;

        if (!Strings.isNullOrEmpty(authorizationHeader)) {
            authenticationDetails = new AuthenticationDetails(authorizationHeader);
        } else if (!Strings.isNullOrEmpty(sessionIdHeader)) {
            // This is used by the legacy web app
            authenticationDetails = new AuthenticationDetails(HttpAuthenticationMethod.SESSION, sessionIdHeader);
        }

        AuthenticationContextRequest requestContext = new AuthenticationContextRequest();
        requestContext.setAuthenticationDetails(authenticationDetails);
        requestContext.setClientKey(clientKey);
        requestContext.setOauth2ClientId(oauth2ClientId);
        requestContext.setRemoteAddress(remoteAddress.orElse(null));
        requestContext.setUserAgent(userAgent);
        requestContext.setUserDeviceId(deviceId);
        requestContext.setHeaders(RequestHeaderUtils.getHeadersMap(httpRequestContext));

        return requestContext;
    }

    private void saveUserIdForAccessLogging(HttpContext context, AuthenticationContext authenticationContext) {
        Map<String, Object> properties = context.getProperties();
        properties.put("userId", authenticationContext.isAuthenticated() ?
                authenticationContext.getUser().getId() : "unauthenticated");
    }

    private class RequestInjectable extends AbstractHttpContextInjectable<AuthenticationContext> {
        private Authenticated authenticated;

        RequestInjectable(Authenticated authenticated) {
            this.authenticated = authenticated;
        }

        @Override
        public AuthenticationContext getValue(HttpContext context) {
            AuthenticationContext authenticationContext = authenticate(context, authenticated);
            saveUserIdForAccessLogging(context, authenticationContext);

            return authenticationContext;
        }
    }

    private class AuthenticatedUserInjectable extends AbstractHttpContextInjectable<AuthenticatedUser> {
        private Authenticated authenticated;

        AuthenticatedUserInjectable(Authenticated authenticated) {
            this.authenticated = authenticated;
        }

        @Override
        public AuthenticatedUser getValue(HttpContext context) {
            DefaultAuthenticationContext requestContext = authenticate(context, authenticated);
            saveUserIdForAccessLogging(context, requestContext);

            if (!requestContext.isAuthenticated()) {
                return null;
            }

            return new AuthenticatedUser(
                    requestContext.getHttpAuthenticationMethod(),
                    requestContext.getOAuth2ClientId().orElse(null),
                    requestContext.getUser(),
                    requestContext.isAdministrativeMode());
        }
    }

    private class UserInjectable extends AbstractHttpContextInjectable<User> {
        private Authenticated authenticated;

        UserInjectable(Authenticated authenticated) {
            this.authenticated = authenticated;
        }

        @Override
        public User getValue(HttpContext context) {
            AuthenticationContext authenticationContext = authenticate(context, authenticated);
            saveUserIdForAccessLogging(context, authenticationContext);

            if (!authenticationContext.isAuthenticated()) {
                return null;
            }

            return authenticationContext.getUser();
        }
    }
}
