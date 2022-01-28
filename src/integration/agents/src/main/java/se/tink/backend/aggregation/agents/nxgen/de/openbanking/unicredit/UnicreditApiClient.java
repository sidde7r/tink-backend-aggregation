package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.rpc.UnicreditConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.payment.rpc.UnicreditCreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.Endpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.PathParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration.UnicreditProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.FinalizeAuthorizationRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.PsuDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.StorageValues;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class UnicreditApiClient extends UnicreditBaseApiClient {

    private static final boolean TPP_REDIRECT_PREFERRED_VALUE = false;
    private static final String EMPTY_BODY = "{}";
    private final SessionStorage sessionStorage;

    UnicreditApiClient(
            TinkHttpClient client,
            UnicreditStorage unicreditStorage,
            UnicreditProviderConfiguration providerConfiguration,
            UnicreditBaseHeaderValues headerValues,
            SessionStorage sessionStorage) {
        super(client, unicreditStorage, providerConfiguration, headerValues);
        this.sessionStorage = sessionStorage;
    }

    @Override
    protected Class<? extends ConsentResponse> getConsentResponseType() {
        return UnicreditConsentResponse.class;
    }

    @Override
    public UnicreditConsentResponse createConsent(String state) {
        return createRequest(new URL(providerConfiguration.getBaseUrl() + Endpoints.CONSENTS))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.PSU_ID_TYPE, providerConfiguration.getPsuIdType())
                // "TPP-Redirect-URI" header is mandatory even if "TPP-Redirect_URI" is set to false
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(headerValues.getRedirectUrl())
                                .queryParam(HeaderKeys.STATE, state)
                                .queryParam(HeaderKeys.CODE, HeaderValues.CODE))
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, TPP_REDIRECT_PREFERRED_VALUE)
                .post(UnicreditConsentResponse.class, getConsentRequest());
    }

    public AuthorizationResponse initializeAuthorization(
            String url, String state, String username) {
        return createRequest(new URL(url))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.PSU_ID, username)
                .header(HeaderKeys.PSU_ID_TYPE, providerConfiguration.getPsuIdType())
                // "TPP-Redirect-URI" header is mandatory even if "TPP-Redirect_URI" is set to false
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(headerValues.getRedirectUrl())
                                .queryParam(HeaderKeys.STATE, state)
                                .queryParam(HeaderKeys.CODE, HeaderValues.CODE))
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, TPP_REDIRECT_PREFERRED_VALUE)
                .post(AuthorizationResponse.class, EMPTY_BODY);
    }

    public AuthorizationResponse authorizeWithPassword(
            String url, String username, String password) {
        return createRequest(new URL(url))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.PSU_ID, username)
                .put(
                        AuthorizationResponse.class,
                        new AuthorizationRequest(new PsuDataEntity(password)));
    }

    public AuthorizationResponse finalizeAuthorization(String url, String otp) {
        return createRequest(new URL(url))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .put(AuthorizationResponse.class, new FinalizeAuthorizationRequest(otp));
    }

    @Override
    public CreatePaymentResponse createSepaPayment(
            CreatePaymentRequest request, PaymentRequest paymentRequest) {
        UnicreditCreatePaymentResponse response =
                createRequest(
                                new URL(
                                                providerConfiguration.getBaseUrl()
                                                        + Endpoints.PAYMENT_INITIATION)
                                        .parameter(
                                                PathParameters.PAYMENT_SERVICE,
                                                getPaymentService(paymentRequest))
                                        .parameter(
                                                PathParameters.PAYMENT_PRODUCT,
                                                getPaymentProduct(paymentRequest)))
                        .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                        .header(HeaderKeys.PSU_ID_TYPE, providerConfiguration.getPsuIdType())
                        .header(
                                HeaderKeys.TPP_REDIRECT_URI,
                                new URL(headerValues.getRedirectUrl())
                                        .queryParam(
                                                HeaderKeys.STATE,
                                                unicreditStorage
                                                        .getAuthenticationState()
                                                        .orElse(null))
                                        .queryParam(HeaderKeys.CODE, HeaderValues.CODE))
                        .header(HeaderKeys.TPP_REDIRECT_PREFERRED, TPP_REDIRECT_PREFERRED_VALUE)
                        .post(UnicreditCreatePaymentResponse.class, request);

        sessionStorage.put(StorageValues.SCA_LINKS, response.getLinks());

        return response;
    }
}
