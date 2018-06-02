package se.tink.backend.main.transports;

import com.google.inject.Inject;
import java.util.Optional;
import javax.security.auth.login.CredentialExpiredException;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import se.tink.backend.api.AuthenticationService;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationDetails;
import se.tink.backend.common.exceptions.DuplicateException;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.core.exceptions.AuthenticationTokenExpiredException;
import se.tink.backend.core.exceptions.AuthenticationTokenNotFoundException;
import se.tink.backend.main.auth.DefaultAuthenticationContext;
import se.tink.backend.main.auth.factories.AuthenticationContextBuilderFactory;
import se.tink.backend.main.controllers.AuthenticationServiceController;
import se.tink.backend.rpc.AuthenticatedLoginResponse;
import se.tink.backend.rpc.AuthenticatedRegisterRequest;
import se.tink.backend.rpc.AuthenticatedRegisterResponse;
import se.tink.backend.rpc.EmailAndPasswordAuthenticationCommand;
import se.tink.backend.rpc.RegisterAccountCommand;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.backend.rpc.auth.EmailAndPasswordAuthenticationRequest;
import se.tink.backend.rpc.auth.bankid.CollectBankIdAuthenticationResponse;
import se.tink.backend.rpc.auth.bankid.InitiateBankIdAuthenticationRequest;
import se.tink.backend.rpc.auth.bankid.InitiateBankIdAuthenticationResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.api.headers.TinkHttpHeaders;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.libraries.validation.exceptions.InvalidEmailException;
import se.tink.libraries.validation.exceptions.InvalidLocaleException;

// TODO: Remove the implementation of Managed when we have Guice lifecycle management.

@Path("/api/v1/authentication")
public class AuthenticationServiceJerseyTransport implements AuthenticationService {

    @Context
    private HttpHeaders headers;

    private static final LogUtils log = new LogUtils(AuthenticationServiceJerseyTransport.class);

    private final AuthenticationServiceController authenticationServiceController;
    private final AuthenticationContextBuilderFactory authenticationContextBuilderFactory;

    @Inject
    public AuthenticationServiceJerseyTransport(AuthenticationServiceController authenticationServiceController,
            AuthenticationContextBuilderFactory authenticationContextBuilderFactory) {
        this.authenticationServiceController = authenticationServiceController;
        this.authenticationContextBuilderFactory = authenticationContextBuilderFactory;
    }

    @Override
    public InitiateBankIdAuthenticationResponse initiateBankIdAuthentication(
            final InitiateBankIdAuthenticationRequest request) {
        final Optional<String> clientKey = Optional
                .ofNullable(RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.CLIENT_KEY_HEADER_NAME));
        final Optional<String> oauth2ClientId = Optional
                .ofNullable(RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.OAUTH_CLIENT_ID_HEADER_NAME));
        final Optional<String> deviceId = Optional
                .ofNullable(RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.DEVICE_ID_HEADER_NAME));
        try {
            Optional<String> nationalId =
                    request != null ? Optional.ofNullable(request.getNationalId()) : Optional.empty();
            Optional<String> market = request != null ? Optional.ofNullable(request.getMarket()) : Optional.empty();
            return authenticationServiceController
                    .initiateBankIdAuthentication(nationalId, market, clientKey, oauth2ClientId, deviceId,
                            Optional.empty());
        } catch (Exception e) {
            log.error("Unable to initiate Bank Id authentication.", e);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    @Override
    public CollectBankIdAuthenticationResponse collectBankIdAuthentication(String authenticationToken) {
        try {
            return authenticationServiceController.collectBankIdAuthentication(authenticationToken);
        } catch (CredentialExpiredException e) {
            log.error("Collect BankId authentication: Authentication token expired.", e);
            throw new WebApplicationException(Response.Status.GONE);
        } catch (Exception e) {
            log.error("Collect BankId authentication: Unable to collect Bank Id authentication.", e);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    @Override
    public AuthenticationResponse emailAndPasswordAuthentication(EmailAndPasswordAuthenticationRequest request) {

        try {
            EmailAndPasswordAuthenticationCommand command = new EmailAndPasswordAuthenticationCommand(
                    request.getEmail(),
                    request.getPassword(),
                    request.getMarket(),
                    RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.CLIENT_KEY_HEADER_NAME),
                    RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.OAUTH_CLIENT_ID_HEADER_NAME),
                    RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.DEVICE_ID_HEADER_NAME),
                    RequestHeaderUtils.getUserAgent(headers));
            return authenticationServiceController.authenticateEmailAndPassword(command);
        } catch (InvalidEmailException e) {
            log.error("Email and password authentication: Invalid email.", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public AuthenticatedLoginResponse login(String authenticationToken) {
        try {
            DefaultAuthenticationContext authenticationContext = getAuthenticationContext(authenticationToken);
            return authenticationServiceController.authenticatedLogin(authenticationToken, authenticationContext);
        } catch (CredentialExpiredException e) {
            log.error("Login: Authentication token expired.", e);
            throw new WebApplicationException(Response.Status.GONE);
        } catch (Exception e) {
            log.error("Login: Unauthorized.", e);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    @Override
    public AuthenticatedRegisterResponse register(String authenticationToken, AuthenticatedRegisterRequest request) {

        try {
            RegisterAccountCommand command = new RegisterAccountCommand(authenticationToken, request.getEmail(),
                    request.getLocale());

            DefaultAuthenticationContext authenticationContext = getAuthenticationContext(authenticationToken);
            String sessionId = authenticationServiceController.authenticatedRegister(authenticationContext, command);

            AuthenticatedRegisterResponse response = new AuthenticatedRegisterResponse();
            response.setSessionId(sessionId);

            return response;
        } catch (InvalidEmailException e) {
            log.error("Register: Invalid email.", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (InvalidLocaleException e) {
            log.error("Register: Invalid locale.", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (AuthenticationTokenExpiredException e) {
            log.error("Register: Authentication token expired.", e);
            throw new WebApplicationException(Response.Status.GONE);
        } catch (DuplicateException e) {
            log.error("Register: User details belongs to another user.", e);
            throw new WebApplicationException(Response.Status.CONFLICT);
        } catch (AuthenticationTokenNotFoundException e) {
            log.error("Register: Authentication token not found.", e);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Register: Unauthorized.", e);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    private DefaultAuthenticationContext getAuthenticationContext(String authenticationId) {
        AuthenticationContextRequest request = new AuthenticationContextRequest();
        request.setAuthenticationDetails(new AuthenticationDetails(HttpAuthenticationMethod.TOKEN, authenticationId));
        request.setRemoteAddress(RequestHeaderUtils.getRemoteIp(headers).orElse(null));
        request.setUserAgent(RequestHeaderUtils.getUserAgent(headers));
        request.setUserDeviceId(RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.DEVICE_ID_HEADER_NAME));
        request.setHeaders(RequestHeaderUtils.getHeadersMap(headers));

        return authenticationContextBuilderFactory.create(request).build();
    }
}
