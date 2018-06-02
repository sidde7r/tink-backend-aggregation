package se.tink.backend.grpc.v1.auth;

import com.google.api.client.util.Strings;
import com.google.inject.Inject;
import io.grpc.Metadata;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationDetails;
import se.tink.backend.auth.AuthenticationRequirements;
import se.tink.backend.grpc.v1.utils.TinkGrpcHeaders;
import se.tink.backend.main.auth.DefaultAuthenticationContext;
import se.tink.backend.main.auth.authenticators.DefaultRequestAuthenticator;

public class GrpcAuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(GrpcAuthenticationProvider.class);
    private final DefaultRequestAuthenticator requestAuthenticator;

    @Inject
    public GrpcAuthenticationProvider(DefaultRequestAuthenticator requestAuthenticator) {
        this.requestAuthenticator = requestAuthenticator;
    }

    public DefaultAuthenticationContext authenticate(Metadata requestHeaders,
            AuthenticationRequirements authenticationRequirements, String remoteAddress)
            throws IllegalAccessException {
        AuthenticationContextRequest authenticationContextRequest = build(requestHeaders, remoteAddress);

        return requestAuthenticator.authenticate(authenticationRequirements, authenticationContextRequest);
    }

    public DefaultAuthenticationContext authenticate(Metadata requestHeaders, Authenticated authenticated,
            String remoteAddress) throws IllegalAccessException {
        return authenticate(requestHeaders, AuthenticationRequirements.fromAuthenticated(authenticated), remoteAddress);
    }

    private AuthenticationContextRequest build(Metadata requestHeaders, String remoteAddress) {
        String authorizationHeader = requestHeaders.get(TinkGrpcHeaders.AUTHORIZATION);
        String clientKey = requestHeaders.get(TinkGrpcHeaders.CLIENT_KEY_HEADER_NAME);
        String oauth2ClientId = requestHeaders.get(TinkGrpcHeaders.OAUTH_CLIENT_ID_HEADER_NAME);
        String deviceId = requestHeaders.get(TinkGrpcHeaders.DEVICE_ID_HEADER_NAME);
        String userAgent = requestHeaders.get(TinkGrpcHeaders.USER_AGENT);

        AuthenticationDetails authenticationDetails = null;
        if (!Strings.isNullOrEmpty(authorizationHeader)) {
            authenticationDetails = new AuthenticationDetails(authorizationHeader);
        }

        AuthenticationContextRequest requestContext = new AuthenticationContextRequest();
        requestContext.setAuthenticationDetails(authenticationDetails);
        requestContext.setClientKey(clientKey);
        requestContext.setOauth2ClientId(oauth2ClientId);
        requestContext.setRemoteAddress(remoteAddress);
        requestContext.setUserAgent(userAgent);
        requestContext.setUserDeviceId(deviceId);
        requestContext.setHeaders(convertHeadersToMap(requestHeaders));

        return requestContext;
    }

    /**
     * Convert the grpc metadata to Strings in the format of key value
     * The filter removes all the binary metadata which we probably don't need.
     */
    private Map<String, String> convertHeadersToMap(Metadata headers) {
        return headers.keys().stream()
                .filter(key -> !key.contains("-bin"))
                .map(key -> Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))
                .collect(Collectors.toMap(Metadata.Key::name, headers::get));
    }

}
