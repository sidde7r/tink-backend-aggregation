package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static java.lang.String.format;
import static java.util.Base64.getUrlEncoder;
import static java.util.Locale.US;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.GET;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.POST;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.PUT;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Value;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.Configuration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.configuration.DkbConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.http.NextGenRequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class DkbAuthRequestsFactory {

    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd";

    private final DkbConfiguration config;
    private final DkbStorage storage;

    public DkbAuthRequestsFactory(DkbConfiguration config, DkbStorage storage) {
        this.config = config;
        this.storage = storage;
    }

    private HttpRequestBuilder newRequest(String urlPath) {
        return getRequestBuilder(config.getBaseUrl() + urlPath)
                .accept(APPLICATION_JSON_TYPE)
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.PSU_IP_ADDRESS, Configuration.LOCALHOST)
                .acceptLanguage(US);
    }

    private HttpRequestBuilder getRequestBuilder(String url) {
        return new NextGenRequestBuilder(null, new URL(url), null, null);
    }

    HttpRequest generateWso2TokenRequest() {
        return newRequest("/oauth2/token")
                .type(APPLICATION_FORM_URLENCODED_TYPE)
                .addBasicAuth(config.getConsumerId(), config.getConsumerSecret())
                .body("grant_type=client_credentials")
                .build(POST);
    }

    HttpRequest generateAuth1stFactorRequest(String username, String password) {
        return newRequest("/pre-auth/psd2-auth/v1/auth/token")
                .type(APPLICATION_JSON_TYPE)
                .addBearerToken(storage.getWso2OAuthToken())
                .header(
                        HeaderKeys.PSD_2_AUTHORIZATION_HEADER,
                        basicAuthValue(config.getClientId(), config.getClientSecret()))
                .body(new UserCredentials(username, password))
                .build(POST);
    }

    HttpRequest generateAuthMethodSelectionRequest(String methodId) {
        return newRequest("/pre-auth/psd2-auth/v1/challenge")
                .type(APPLICATION_JSON_TYPE)
                .addBearerToken(storage.getWso2OAuthToken())
                .header(
                        HeaderKeys.PSD_2_AUTHORIZATION_HEADER,
                        basicAuthValue(config.getClientId(), config.getClientSecret()))
                .cookie("JSESSIONID", storage.getJsessionid())
                .header("X-XSRF-TOKEN", storage.getXsrfToken())
                .body(new SelectedAuthMethod(methodId))
                .build(POST);
    }

    HttpRequest generateTanSubmissionRequest(String code) {
        return newRequest("/pre-auth/psd2-auth/v1/challenge")
                .type(APPLICATION_JSON_TYPE)
                .addBearerToken(storage.getWso2OAuthToken())
                .header(
                        HeaderKeys.PSD_2_AUTHORIZATION_HEADER,
                        basicAuthValue(config.getClientId(), config.getClientSecret()))
                .cookie("JSESSIONID", storage.getJsessionid())
                .header("X-XSRF-TOKEN", storage.getXsrfToken())
                .body(new TanCode(code))
                .build(PUT);
    }

    HttpRequest generateCreateConsentRequest(LocalDate validUntil) {
        return newRequest("/psd2/v1/consents")
                .type(APPLICATION_JSON_TYPE)
                .addBearerToken(storage.getWso2OAuthToken())
                .header(
                        HeaderKeys.PSD_2_AUTHORIZATION_HEADER,
                        storage.getAccessToken().toAuthorizeHeader())
                .body(new Consents(validUntil))
                .build(POST);
    }

    HttpRequest generateGetConsentRequest(String consentId) {
        return newRequest(format("/psd2/v1/consents/%s", consentId))
                .type(APPLICATION_JSON_TYPE)
                .addBearerToken(storage.getWso2OAuthToken())
                .header(
                        HeaderKeys.PSD_2_AUTHORIZATION_HEADER,
                        storage.getAccessToken().toAuthorizeHeader())
                .build(GET);
    }

    HttpRequest generateConsentAuthorizationRequest(String consentId) {
        return newRequest(format("/psd2/v1/consents/%s/authorisations", consentId))
                .type(APPLICATION_JSON_TYPE)
                .addBearerToken(storage.getWso2OAuthToken())
                .body("{}")
                .header(
                        HeaderKeys.PSD_2_AUTHORIZATION_HEADER,
                        storage.getAccessToken().toAuthorizeHeader())
                .build(POST);
    }

    private String getConstantAuthorizationUrl(String consentId, String authorizationId) {
        return format("/psd2/v1/consents/%s/authorisations/%s", consentId, authorizationId);
    }

    HttpRequest generateConsentAuthorizationMethodRequest(
            String consentId, String authorizationId, String methodId) {
        return newRequest(getConstantAuthorizationUrl(consentId, authorizationId))
                .type(APPLICATION_JSON_TYPE)
                .addBearerToken(storage.getWso2OAuthToken())
                .header(
                        HeaderKeys.PSD_2_AUTHORIZATION_HEADER,
                        storage.getAccessToken().toAuthorizeHeader())
                .body(new ConsentAuthorizationMethod(methodId))
                .build(PUT);
    }

    HttpRequest generateConsentAuthorizationOtpRequest(
            String consentId, String authorizationId, String otp) {
        return newRequest(getConstantAuthorizationUrl(consentId, authorizationId))
                .type(APPLICATION_JSON_TYPE)
                .addBearerToken(storage.getWso2OAuthToken())
                .header(
                        HeaderKeys.PSD_2_AUTHORIZATION_HEADER,
                        storage.getAccessToken().toAuthorizeHeader())
                .body(new ConsentAuthorizationOtp(otp))
                .build(PUT);
    }

    private String basicAuthValue(String username, String password) {
        return format(
                "Basic %s",
                getUrlEncoder().encodeToString(format("%s:%s", username, password).getBytes()));
    }

    @Value
    @JsonObject
    static class UserCredentials {

        private String username;
        private String password;
    }

    @Value
    @JsonObject
    static class SelectedAuthMethod {

        private String authOptionId;
    }

    @Value
    @JsonObject
    static class TanCode {

        private String tan;
    }

    @JsonObject
    @Getter
    @Value
    static class Consents {

        private Access access = new Access();
        private boolean recurringIndicator = true;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ISO_DATE_FORMAT)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate validUntil;

        private Integer frequencyPerDay = 4;
        private boolean combinedServiceIndicator = false;

        @Value
        @JsonObject
        static class Access {

            private String allPsd2 = "allAccounts";
        }
    }

    @JsonObject
    @Value
    static class ConsentAuthorizationMethod {

        private final String authenticationMethodId;
    }

    @JsonObject
    @Value
    static class ConsentAuthorizationOtp {

        @JsonProperty("scaAuthenticationData")
        private final String otp;
    }
}
