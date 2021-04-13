package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static java.lang.String.format;
import static java.util.Locale.US;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.GET;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.POST;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.PUT;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.Value;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbUserIpInformation;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.configuration.DkbConfiguration;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.NextGenRequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class DkbAuthRequestsFactory {

    private final DkbConfiguration config;
    private final DkbStorage storage;
    private final DkbUserIpInformation dkbUserIpInformation;

    public DkbAuthRequestsFactory(
            DkbConfiguration config,
            DkbStorage storage,
            DkbUserIpInformation dkbUserIpInformation) {
        this.config = config;
        this.storage = storage;
        this.dkbUserIpInformation = dkbUserIpInformation;
    }

    private HttpRequestBuilder newRequest(String urlPath) {
        return getRequestBuilder(config.getBaseUrl() + urlPath)
                .accept(APPLICATION_JSON_TYPE)
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.PSU_IP_ADDRESS, dkbUserIpInformation.getUserIp())
                .acceptLanguage(US);
    }

    private HttpRequestBuilder getRequestBuilder(String url) {
        return new NextGenRequestBuilder(null, new URL(url), null, null);
    }

    HttpRequest generateAuth1stFactorRequest(String username, String password) {
        return newRequest("/pre-auth/psd2-auth/v1/auth/token")
                .type(APPLICATION_JSON_TYPE)
                .body(new UserCredentials(username, password))
                .build(POST);
    }

    HttpRequest generateAuthMethodSelectionRequest(String methodId) {
        return newRequest("/pre-auth/psd2-auth/v1/challenge")
                .type(APPLICATION_JSON_TYPE)
                .cookie("JSESSIONID", storage.getJsessionid())
                .header("X-XSRF-TOKEN", storage.getXsrfToken())
                .body(new SelectedAuthMethod(methodId))
                .build(POST);
    }

    HttpRequest generateTanSubmissionRequest(String code) {
        return newRequest("/pre-auth/psd2-auth/v1/challenge")
                .type(APPLICATION_JSON_TYPE)
                .cookie("JSESSIONID", storage.getJsessionid())
                .header("X-XSRF-TOKEN", storage.getXsrfToken())
                .body(new TanCode(code))
                .build(PUT);
    }

    HttpRequest generateCreateConsentRequest(LocalDate validUntil) {
        ConsentRequest consentRequest =
                new ConsentRequest(
                        new AccessEntity("allAccounts"), true, validUntil.toString(), 4, false);
        return newRequest("/psd2/v1/consents")
                .type(APPLICATION_JSON_TYPE)
                .header(
                        HeaderKeys.PSD_2_AUTHORIZATION_HEADER,
                        storage.getAccessToken().map(OAuth2Token::toAuthorizeHeader).orElse(null))
                .body(consentRequest)
                .build(POST);
    }

    HttpRequest generateGetConsentRequest(String consentId) {
        return newRequest(format("/psd2/v1/consents/%s", consentId))
                .type(APPLICATION_JSON_TYPE)
                .header(
                        HeaderKeys.PSD_2_AUTHORIZATION_HEADER,
                        storage.getAccessToken().map(OAuth2Token::toAuthorizeHeader).orElse(null))
                .build(GET);
    }

    HttpRequest generateConsentAuthorizationRequest(String consentId) {
        return newRequest(format("/psd2/v1/consents/%s/authorisations", consentId))
                .type(APPLICATION_JSON_TYPE)
                .body("{}")
                .header(
                        HeaderKeys.PSD_2_AUTHORIZATION_HEADER,
                        storage.getAccessToken().map(OAuth2Token::toAuthorizeHeader).orElse(null))
                .build(POST);
    }

    private String getConstantAuthorizationUrl(String consentId, String authorizationId) {
        return format("/psd2/v1/consents/%s/authorisations/%s", consentId, authorizationId);
    }

    HttpRequest generateConsentAuthorizationMethodRequest(
            String consentId, String authorizationId, String methodId) {
        return newRequest(getConstantAuthorizationUrl(consentId, authorizationId))
                .type(APPLICATION_JSON_TYPE)
                .header(
                        HeaderKeys.PSD_2_AUTHORIZATION_HEADER,
                        storage.getAccessToken().map(OAuth2Token::toAuthorizeHeader).orElse(null))
                .body(new ConsentAuthorizationMethod(methodId))
                .build(PUT);
    }

    HttpRequest generateConsentAuthorizationOtpRequest(
            String consentId, String authorizationId, String otp) {
        return newRequest(getConstantAuthorizationUrl(consentId, authorizationId))
                .type(APPLICATION_JSON_TYPE)
                .header(
                        HeaderKeys.PSD_2_AUTHORIZATION_HEADER,
                        storage.getAccessToken().map(OAuth2Token::toAuthorizeHeader).orElse(null))
                .body(new ConsentAuthorizationOtp(otp))
                .build(PUT);
    }

    @Value
    @JsonObject
    static class UserCredentials {

        String username;
        String password;
    }

    @Value
    @JsonObject
    static class SelectedAuthMethod {

        String authOptionId;
    }

    @Value
    @JsonObject
    static class TanCode {

        String tan;
    }

    @JsonObject
    @Value
    static class ConsentAuthorizationMethod {

        String authenticationMethodId;
    }

    @JsonObject
    @Value
    static class ConsentAuthorizationOtp {

        @JsonProperty("scaAuthenticationData")
        String otp;
    }
}
