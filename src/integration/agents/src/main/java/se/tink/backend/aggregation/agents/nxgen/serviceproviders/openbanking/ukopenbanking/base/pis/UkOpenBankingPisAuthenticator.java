package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAisAuthenticatorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPis;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.ClientMode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UkOpenBankingPisAuthenticator implements OpenIdAuthenticator {
    private final UkOpenBankingApiClient apiClient;
    private final SoftwareStatementAssertion softwareStatement;
    private final ProviderConfiguration providerConfiguration;
    private final UkOpenBankingPis ukOpenBankingPis;
    private PaymentResponse paymentResponse;
    private final PaymentRequest paymentRequest;

    private String intentId;

    public UkOpenBankingPisAuthenticator(
            UkOpenBankingApiClient apiClient,
            SoftwareStatementAssertion softwareStatement,
            ProviderConfiguration providerConfiguration,
            UkOpenBankingPis ukOpenBankingPis,
            PaymentRequest paymentRequest) {
        this.apiClient = apiClient;
        this.softwareStatement = softwareStatement;
        this.providerConfiguration = providerConfiguration;
        this.ukOpenBankingPis = ukOpenBankingPis;
        this.paymentRequest = paymentRequest;
    }

    public Optional<String> getIntentId() {
        return Optional.ofNullable(intentId);
    }

    public PaymentResponse getPaymentResponse() {
        return paymentResponse;
    }

    @Override
    public URL decorateAuthorizeUrl(
            URL authorizeUrl, String state, String nonce, String callbackUri) {

        paymentResponse = ukOpenBankingPis.setupPaymentOrderConsent(apiClient, paymentRequest);
        intentId = paymentResponse.getStorage().get("consentId");

        WellKnownResponse wellKnownConfiguration = apiClient.getWellKnownConfiguration();

        return authorizeUrl.queryParam(
                UkOpenBankingAisAuthenticatorConstants.Params.REQUEST,
                AuthorizeRequest.create()
                        .withClientInfo(providerConfiguration.getClientInfo())
                        .withPaymentsScope()
                        .withSoftwareStatement(softwareStatement)
                        .withRedirectUrl(apiClient.getRedirectUrl())
                        .withState(state)
                        .withNonce(nonce)
                        .withWellknownConfiguration(wellKnownConfiguration)
                        .withIntentId(intentId)
                        .build(apiClient.getSigner()));
    }

    @Override
    public ClientMode getClientCredentialScope() {
        return ClientMode.PAYMENTS;
    }
}
