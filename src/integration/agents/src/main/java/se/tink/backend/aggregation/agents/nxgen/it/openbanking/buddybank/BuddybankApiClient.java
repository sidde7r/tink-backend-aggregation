package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc.BuddybankConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.payment.executor.rpc.BuddybankCreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration.UnicreditProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.CreatePaymentResponse;
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

    @Override
    public ConsentResponse createConsent(String state) {
        ConsentResponse consentResponse = super.createConsent(state);
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
}
