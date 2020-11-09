package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAisAuthenticatorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.ClientMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.entities.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class UkOpenBankingPisAuthenticator implements UkOpenBankingAuthenticator {

    private final UkOpenBankingApiClient apiClient;
    private final UkOpenbankingPaymentHelper paymentHelper;
    private final SoftwareStatementAssertion softwareStatement;
    private final ClientInfo clientInfo;
    private PaymentResponse paymentResponse;
    private final PaymentRequest paymentRequest;

    public PaymentResponse getPaymentResponse() {
        return paymentResponse;
    }

    @Override
    public URL decorateAuthorizeUrl(
            URL authorizeUrl, String state, String nonce, String callbackUri) {
        paymentResponse = paymentHelper.createConsent(paymentRequest);
        String intentId = paymentResponse.getStorage().get("consentId");

        WellKnownResponse wellKnownConfiguration = apiClient.getWellKnownConfiguration();

        return authorizeUrl.queryParam(
                UkOpenBankingAisAuthenticatorConstants.Params.REQUEST,
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
}
