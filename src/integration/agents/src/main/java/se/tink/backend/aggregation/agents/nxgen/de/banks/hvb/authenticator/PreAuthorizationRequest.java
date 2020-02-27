package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationRequest.RequestBody.ChallengeResponse.UserLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationRequest.ResponseBody.Successes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationRequest.ResponseBody.Successes.UserLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationRequest.ResponseBody.Successes.UserLoginResponse.User;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationRequest.ResponseBody.Successes.UserLoginResponse.User.Attributes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.scaffold.SimpleExternalApiCall;

public class PreAuthorizationRequest extends SimpleExternalApiCall<AuthenticationData, String> {

    private final ConfigurationProvider configurationProvider;

    public PreAuthorizationRequest(
            TinkHttpClient httpClient, ConfigurationProvider configurationProvider) {
        super(httpClient);
        this.configurationProvider = configurationProvider;
    }

    @Override
    protected HttpRequest prepareRequest(AuthenticationData authData) {
        return new HttpRequestImpl(
                HttpMethod.POST,
                new URL(configurationProvider.getBaseUrl() + "/preauth/v1/preauthorize"),
                prepareRequestHeaders(authData),
                prepareRequestBody(authData));
    }

    private MultivaluedMap<String, Object> prepareRequestHeaders(AuthenticationData authData) {
        MultivaluedMap<String, Object> headers = configurationProvider.getStaticHeaders();
        headers.putSingle(ACCEPT, "text/javascript, text/html, application/xml, text/xml, */*");
        headers.putSingle(CONTENT_TYPE, APPLICATION_JSON);
        headers.putSingle("x-wl-app-version", "4.1.0");
        headers.putSingle("AvantiPIN", authData.getPin());
        headers.putSingle(
                "AvantiSec",
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        return headers;
    }

    private RequestBody prepareRequestBody(AuthenticationData authData) {
        return RequestBody.of(
                authData.getClientId(),
                authData.getUserId(),
                authData.getPin(),
                authData.getApplicationSessionId());
    }

    @Override
    protected ExternalApiCallResult<String> parseResponse(HttpResponse httpResponse) {
        ResponseBody responseBody = httpResponse.getBody(ResponseBody.class);
        return ExternalApiCallResult.of(getSessionId(responseBody), httpResponse.getStatus());
    }

    private String getSessionId(ResponseBody responseBody) {
        return Optional.ofNullable(responseBody)
                .map(ResponseBody::getSuccesses)
                .map(Successes::getUserLogin)
                .map(UserLoginResponse::getUser)
                .map(User::getAttributes)
                .map(Attributes::getSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Couldn't obtain session id"));
    }

    @Data
    @JsonObject
    @Accessors(chain = true)
    static class RequestBody {
        @JsonProperty("client_id")
        private String clientId;

        private String scope = "RegisteredClient UCAuthenticatedUser";
        private ChallengeResponse challengeResponse;

        static RequestBody of(
                String clientId, String uid, String pin, String applicationSessionId) {
            return new RequestBody()
                    .setClientId(clientId)
                    .setChallengeResponse(
                            new ChallengeResponse()
                                    .setUserLogin(
                                            new UserLoginRequest()
                                                    .setUid(uid)
                                                    .setPin(pin)
                                                    .setApplicationSessionId(
                                                            applicationSessionId)));
        }

        @Data
        @JsonObject
        @Accessors(chain = true)
        static class ChallengeResponse {

            @JsonProperty("UCUserAvantiLoginSC")
            private UserLoginRequest userLogin;

            @Data
            @JsonObject
            @Accessors(chain = true)
            static class UserLoginRequest {
                private String uid;
                private String pin;
                private String checkType = "login";
                private String ip = "127.0.0.1";
                private String ipMsite = "127.0.0.1";
                private String platform = "Avanti";
                private String wlVersion = "4.1.0";
                private String environment = "HV";
                private String applicationSessionId = "336692e6";
                private String httpAccept = "*";
                private String httpAcceptEncoding = "*";
                private String httpAcceptLanguage = "*";
                private String httpReferrer = "*";
                private String userAgent = "Mozilla";
                private String operatingSystem = "iOS";
                private String osVersion = "12.4.3";
            }
        }
    }

    @Data
    @JsonObject
    @Accessors(chain = true)
    static class ResponseBody {
        private Successes successes;

        @Data
        @JsonObject
        @Accessors(chain = true)
        static class Successes {
            @JsonProperty("UCUserAvantiLoginSC")
            private UserLoginResponse userLogin;

            private ClockSynchronization clockSynchronization;

            @Data
            @JsonObject
            @Accessors(chain = true)
            static class UserLoginResponse {
                private User user;

                @Data
                @JsonObject
                @Accessors(chain = true)
                static class User {
                    private String id;
                    private String displayName;

                    private Timestamp authenticatedAt;
                    private String authenticatedBy;
                    private Attributes attributes;

                    @Data
                    @JsonObject
                    @Accessors(chain = true)
                    static class Attributes {
                        private String country;
                        private String sessionId;
                    }
                }
            }

            @Data
            @JsonObject
            @Accessors(chain = true)
            static class ClockSynchronization {
                private Timestamp serverTimeStamp;
            }
        }
    }
}
