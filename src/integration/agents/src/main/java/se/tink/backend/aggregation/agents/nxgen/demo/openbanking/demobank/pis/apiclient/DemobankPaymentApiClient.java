package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient;

import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.OAuth2Params.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.OAuth2Params.CLIENT_SECRET;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParams.GRANT_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParams.PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParams.PAYMENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParams.PAYMENT_SERVICE_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParams.SCOPE;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParams.USERNAME;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls.BASE_URL;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls.OAUTH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls.SIGN_PAYMENT;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.GrantTypes;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.PaymentServiceTypes;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ScaApproach;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Scopes;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.RedirectLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.DemobankDtoMappers;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.AuthorisationInitiationDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.AuthorisationResponseDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.AuthorizationRequestDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.LinksDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.error.DemobankErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.storage.DemobankStorage;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payments.common.model.PaymentScheme;

@RequiredArgsConstructor
public abstract class DemobankPaymentApiClient {
    protected final DemobankDtoMappers mappers;
    protected final DemobankErrorHandler errorHandler;
    protected final DemobankPaymentRequestFilter requestFilter;
    protected final DemobankStorage storage;
    protected final TinkHttpClient client;
    protected final String callbackUri;

    private static final String DEFAULT_PAYMENT_SCHEME = "SEPA_CREDIT_TRANSFER";

    public abstract PaymentResponse createPayment(PaymentRequest paymentRequest)
            throws PaymentException;

    public abstract PaymentResponse getPayment(String paymentId);

    public abstract PaymentStatus getPaymentStatus(String paymentId);

    public OAuth2Token exchangeAccessCode(String code) {
        return client.request(OAUTH_TOKEN)
                .addBasicAuth(CLIENT_ID, CLIENT_SECRET)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(TokenEntity.class, new RedirectLoginRequest(code, callbackUri).toData())
                .toOAuth2Token();
    }

    public OAuth2Token loginUser(String username, String password) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(USERNAME, username);
        queryParams.put(PASSWORD, password);
        queryParams.put(GRANT_TYPE, GrantTypes.PASSWORD);
        queryParams.put(SCOPE, Scopes.PAYMENT_WRITE);

        return client.request(OAUTH_TOKEN)
                .queryParams(queryParams)
                .addBasicAuth(CLIENT_ID, CLIENT_SECRET)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(TokenEntity.class)
                .toOAuth2Token();
    }

    public AuthorisationResponseDto startOtpAuthorisation(String authorisationUri) {
        URL url = BASE_URL.concat(authorisationUri);
        return client.request(url)
                .header(HttpHeaders.AUTHORIZATION, storage.getAccessToken().toAuthorizeHeader())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(
                        AuthorisationResponseDto.class,
                        new AuthorisationInitiationDto(ScaApproach.OTP));
    }

    public void signPaymentWithOtp(String otp) {
        AuthorizationRequestDto authorizationRequestDto = new AuthorizationRequestDto(otp);
        String paymentId = storage.getPaymentId();
        String authToken = storage.getAccessToken().toAuthorizeHeader();

        client.request(
                        SIGN_PAYMENT
                                .parameter(PAYMENT_SERVICE_TYPE, PaymentServiceTypes.PAYMENTS)
                                .parameter(PAYMENT_ID, paymentId))
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(authorizationRequestDto);
    }

    public void saveLinksToStorage(String paymentId, LinksDto links) {
        final String authorizeUrl =
                Optional.ofNullable(links)
                        .map(LinksDto::getScaRedirect)
                        .map(Href::getHref)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                ErrorMessages.RESPONSE_DOES_NOT_CONTAIN_SCA_LINK));

        final String embeddedAuthorizeUrl =
                Optional.ofNullable(links)
                        .map(LinksDto::getAuthoriseTransaction)
                        .map(Href::getHref)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                ErrorMessages
                                                        .RESPONSE_DOES_NOT_CONTAIN_EMBEDDED_AUTH_LINK));

        storage.storePaymentId(paymentId);
        storage.storeAuthorizeUrl(authorizeUrl);
        storage.storeEmbeddedAuthorizeUrl(embeddedAuthorizeUrl);
    }

    static String getPaymentScheme(PaymentRequest paymentRequest) {
        return convertSchemeToKebabCase(
                Optional.ofNullable(paymentRequest.getPayment().getPaymentScheme())
                        .map(PaymentScheme::toString)
                        .orElse(DEFAULT_PAYMENT_SCHEME));
    }

    private static String convertSchemeToKebabCase(String scheme) {
        return scheme.replace("_", "-").toLowerCase();
    }
}
