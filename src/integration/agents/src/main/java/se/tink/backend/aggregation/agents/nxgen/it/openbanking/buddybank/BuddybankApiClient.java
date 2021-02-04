package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank;

import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc.BuddybankConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc.BuddybankCreateConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.payment.executor.rpc.BuddybankCreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.payment.executor.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.Endpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration.UnicreditProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BuddybankApiClient extends UnicreditBaseApiClient {

    public BuddybankApiClient(
            TinkHttpClient client,
            UnicreditStorage unicreditStorage,
            UnicreditProviderConfiguration providerConfiguration,
            UnicreditBaseHeaderValues headerValues) {
        super(client, unicreditStorage, providerConfiguration, headerValues);
    }

    public BuddybankCreateConsentResponse createBuddybankConsent(String state) {
        BuddybankCreateConsentResponse consentResponse =
                createRequest(new URL(providerConfiguration.getBaseUrl() + Endpoints.CONSENTS))
                        .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                        .header(HeaderKeys.PSU_ID_TYPE, providerConfiguration.getPsuIdType())
                        .header(
                                HeaderKeys.TPP_REDIRECT_URI,
                                new URL(headerValues.getRedirectUrl())
                                        .queryParam(HeaderKeys.STATE, state)
                                        .queryParam(HeaderKeys.CODE, HeaderValues.CODE))
                        .header(HeaderKeys.TPP_REDIRECT_PREFERED, true)
                        .post(BuddybankCreateConsentResponse.class, getConsentRequest());

        unicreditStorage.saveConsentId(consentResponse.getConsentId());

        return consentResponse;
    }

    @Override
    protected Class<? extends ConsentResponse> getConsentResponseType() {
        return BuddybankConsentResponse.class;
    }

    @Override
    public URL getScaRedirectUrlFromConsentResponse(ConsentResponse consentResponse) {
        return new URL(providerConfiguration.getBaseUrl() + consentResponse.getScaRedirect());
    }

    @Override
    protected Class<? extends CreatePaymentResponse> getCreatePaymentResponseType() {
        return BuddybankCreatePaymentResponse.class;
    }

    @Override
    protected String getScaRedirectUrlFromCreatePaymentResponse(
            CreatePaymentResponse consentResponse) {
        return providerConfiguration.getBaseUrl() + consentResponse.getScaRedirect();
    }

    public PaymentStatusResponse getConsentStatus(URL url) {
        return createRequest(url)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .get(PaymentStatusResponse.class);
    }
}
