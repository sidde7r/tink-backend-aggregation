package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.entities.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper.UkOpenBankingPaymentHelper;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class OpenIdPisAuthenticator implements OpenIdAuthenticator {

    private final UkOpenBankingPaymentApiClient apiClient;
    private final UkOpenBankingPaymentHelper paymentHelper;
    private final SoftwareStatementAssertion softwareStatement;
    private final ClientInfo clientInfo;
    private final PaymentRequest paymentRequest;
    private PaymentResponse paymentResponse;

    public PaymentResponse getPaymentResponse() {
        return paymentResponse;
    }

    @Override
    public URL decorateAuthorizeUrl(
            URL authorizeUrl, String state, String nonce, String callbackUri) {
        createConsentWithRetry(paymentRequest);
        String intentId = paymentResponse.getStorage().get("consentId");

        WellKnownResponse wellKnownConfiguration = apiClient.getWellKnownConfiguration();

        return authorizeUrl.queryParam(
                "request",
                AuthorizeRequest.create()
                        .withClientInfo(clientInfo)
                        .withPaymentsScope()
                        .withSoftwareStatement(softwareStatement)
                        .withRedirectUrl(apiClient.getRedirectUrl())
                        .withState(state)
                        .withNonce(nonce)
                        .withWellKnownConfiguration(wellKnownConfiguration)
                        .withIntentId(intentId)
                        .build(apiClient.getSigner()));
    }

    @Override
    public ClientMode getClientCredentialScope() {
        return ClientMode.PAYMENTS;
    }

    /**
     * For fixing the Barclays unstable issue; No-sleep retry had been tested but working not well;
     * No-sleep retry will get continuous rejection; Jira had been raised on UKOB directory by other
     * TPPs
     *
     * @param paymentRequest the Payment Request from Aggregation
     */
    private void createConsentWithRetry(PaymentRequest paymentRequest) {
        for (int i = 0; i < 3; i++) {
            try {
                paymentResponse = paymentHelper.createConsent(paymentRequest);
            } catch (HttpResponseException e) {
                Uninterruptibles.sleepUninterruptibly(2000 * i, TimeUnit.MILLISECONDS);
                continue;
            }
            return;
        }
        paymentResponse = paymentHelper.createConsent(paymentRequest);
    }
}
