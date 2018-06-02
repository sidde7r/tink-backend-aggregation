package se.tink.backend.exception.jersey;

import javax.ws.rs.core.Response;
import se.tink.libraries.oauth.GrantType;

/**
 * General class for throwing Jersey errors on the Main API with corresponding HTTP status codes. These can then be
 * catched by a request error mapper so the API consumer can see the error messages as a response to the API request.
 * The errors should be thrown in the transport layer, and not inside controllers or deeper down in the logic, since
 * they are Jersey specific and would not work with for example gRPC.
 *
 * NOTE: try not to change the error codes, since they can be used by API consumers to identify the type of error.
 */
public class JerseyRequestError {

    private final String errorMessage;
    private String logMessage;
    private Response.Status status;
    private String errorCode;

    JerseyRequestError(String errorMessage, Response.Status status, String errorCode) {
        this.errorMessage = errorMessage;
        this.logMessage = errorMessage;
        this.status = status;
        this.errorCode = errorCode;
    }

    public static class Oauth {
        public static JerseyRequestError NO_CLIENT_ID_IN_REQUEST = new JerseyRequestError("Did not receive a client id in the request", Response.Status.UNAUTHORIZED, "oauth.no_client_id_in_request"); // Change to BAD_REQUEST.
        public static JerseyRequestError OAUTH_CLIENT_NOT_FOUND = new JerseyRequestError("Could not find the OAuth client", Response.Status.UNAUTHORIZED, "oauth.client_not_found"); // Change to NOT_FOUND.
        public static JerseyRequestError REFRESH_TOKEN_NOT_FOUND = new JerseyRequestError("Refresh token not found", Response.Status.UNAUTHORIZED, "oauth.refresh_token_not_found");
        public static JerseyRequestError CODE_NOT_FOUND = new JerseyRequestError("Authorization code not found", Response.Status.UNAUTHORIZED, "oauth.code_not_found");
        public static JerseyRequestError CLIENT_ID_DOES_NOT_MATCH_CODE = new JerseyRequestError("The client id in the request does't match the client id for the code", Response.Status.UNAUTHORIZED, "oauth.client_id_does_not_match_code");
        public static JerseyRequestError CLIENT_ID_DOES_NOT_MATCH_REFRESH = new JerseyRequestError("The client id in the request does't match the client id for the refresh token", Response.Status.UNAUTHORIZED, "oauth.client_id_does_not_match_refresh");
        public static JerseyRequestError INVALID_SCOPE = new JerseyRequestError("Requested scope is not among valid scopes", Response.Status.UNAUTHORIZED, "oauth.invalid_scope");
        public static JerseyRequestError INVALID_REDIRECT_URI = new JerseyRequestError("The redirect URI is invalid", Response.Status.UNAUTHORIZED, "oauth.invalid_redirect_uri");
        public static JerseyRequestError INVALID_SECRET = new JerseyRequestError("Invalid client secret", Response.Status.UNAUTHORIZED, "oauth.invalid_secret");
        public static JerseyRequestError INVALID_GRANT_TYPE = new JerseyRequestError("Grant type must be one of: " + GrantType.DOCUMENTED, Response.Status.UNAUTHORIZED, "oauth.invalid_grant_type");
        public static JerseyRequestError AUTO_AUTH_GRANT_TYPE_NOT_ALLOWED = new JerseyRequestError("For auto-authorize clients we only allow grant_type authorization_code", Response.Status.UNAUTHORIZED, "oauth.auto_auth_grant_type_not_allowed");
    }

    public static class User {
        public static JerseyRequestError ALREADY_EXISTS = new JerseyRequestError("The user already exists", Response.Status.CONFLICT, "user.already_exists");
        public static JerseyRequestError INVALID_EMAIL = new JerseyRequestError("The email is invalid", Response.Status.BAD_REQUEST, "user.invalid_email");
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public Response.Status getStatus() {
        return status;
    }

    public JerseyRequestException exception() {
        return new JerseyRequestException(this);
    }
}
