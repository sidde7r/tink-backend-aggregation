package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank;

import java.util.UUID;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc.BuddybankConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.payment.executor.rpc.BuddybankCreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.payment.executor.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BuddybankApiClient extends UnicreditBaseApiClient {

    public BuddybankApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Credentials credentials,
            boolean manualRequest) {
        super(client, persistentStorage, credentials, manualRequest);
    }

    @Override
    protected Class<? extends ConsentResponse> getConsentResponseType() {
        return BuddybankConsentResponse.class;
    }

    @Override
    protected URL getScaRedirectUrlFromConsentResponse(ConsentResponse consentResponse) {
        return new URL(getConfiguration().getBaseUrl() + consentResponse.getScaRedirect());
    }

    @Override
    protected String getTransactionsDateFrom() {
        return QueryValues.TRANSACTION_FROM_DATE;
    }

    @Override
    protected Class<? extends CreatePaymentResponse> getCreatePaymentResponseType() {
        return BuddybankCreatePaymentResponse.class;
    }

    @Override
    protected String getScaRedirectUrlFromCreatePaymentResponse(
            CreatePaymentResponse consentResponse) {
        return getConfiguration().getBaseUrl() + consentResponse.getScaRedirect();
    }

    public PaymentStatusResponse getConsentStatus(URL url) {
        return createRequest(url)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .get(PaymentStatusResponse.class);
    }
}
