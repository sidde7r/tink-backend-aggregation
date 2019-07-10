package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.executor.payment;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.BerlinGroupPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.BerlinGroupBasePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.http.URL;

public class KbcPaymentExecutor extends BerlinGroupBasePaymentExecutor {

    private final KbcApiClient apiClient;
    private final BerlinGroupPaymentAuthenticator paymentAuthenticator;
    private final BerlinGroupConfiguration configuration;

    public KbcPaymentExecutor(
            KbcApiClient apiClient,
            BerlinGroupPaymentAuthenticator paymentAuthenticator,
            BerlinGroupConfiguration configuration) {
        this.apiClient = apiClient;
        this.paymentAuthenticator = paymentAuthenticator;
        this.configuration = configuration;
    }

    @Override
    protected CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, BerlinGroupPaymentType paymentType) {
        return apiClient.createPayment(createPaymentRequest, paymentType);
    }

    @Override
    protected GetPaymentStatusResponse getPaymentStatus(
            String paymentId, BerlinGroupPaymentType paymentType) {
        return apiClient.getPaymentStatus(paymentId, paymentType);
    }

    @Override
    protected GetPaymentStatusResponse authorizePayment(
            String paymentId, BerlinGroupPaymentType paymentType) {
        paymentAuthenticator.openThirdPartyApp(
                new URL(configuration.getBaseUrl() + Urls.AUTHORIZE_PAYMENT)
                        .parameter(IdTags.PAYMENT_ID, paymentId));

        return getPaymentStatus(paymentId, paymentType);
    }

    @Override
    protected BerlinGroupPaymentType getPaymentType(PaymentRequest paymentRequest) {
        return BerlinGroupPaymentType.SepaCreditTransfers;
    }
}
