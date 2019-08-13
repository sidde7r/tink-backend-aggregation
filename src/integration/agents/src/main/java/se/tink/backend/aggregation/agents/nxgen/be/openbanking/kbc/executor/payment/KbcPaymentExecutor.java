package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.executor.payment;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.BerlinGroupPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.BerlinGroupBasePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.entites.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.entites.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentStatus;

public class KbcPaymentExecutor extends BerlinGroupBasePaymentExecutor {

    private final KbcApiClient apiClient;
    private final BerlinGroupPaymentAuthenticator paymentAuthenticator;
    private final BerlinGroupConfiguration configuration;
    private final SessionStorage sessionStorage;

    public KbcPaymentExecutor(
            KbcApiClient apiClient,
            BerlinGroupPaymentAuthenticator paymentAuthenticator,
            BerlinGroupConfiguration configuration,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.paymentAuthenticator = paymentAuthenticator;
        this.configuration = configuration;
        this.sessionStorage = sessionStorage;
    }

    @Override
    protected CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, BerlinGroupPaymentType paymentType) {
        // It should use create function. This createPayment had to be overriden because of
        // BerlinGroup
        throw new IllegalStateException(ErrorMessages.WRONG_PAYMENT_METHOD);
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
        return BerlinGroupPaymentType.SEPA_CREDIT_TRANSFERS;
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        paymentMultiStepRequest.getPayment().setStatus(PaymentStatus.SIGNED);
        getPaymentStatus(
                paymentMultiStepRequest.getPayment().getUniqueId(),
                BerlinGroupPaymentType.SEPA_CREDIT_TRANSFERS);

        return new PaymentMultiStepResponse(
                paymentMultiStepRequest.getPayment(),
                AuthenticationStepConstants.STEP_FINALIZE,
                null);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);
        AmountEntity amount = AmountEntity.amountOf(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withAmount(amount)
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withCreditorName(paymentRequest.getPayment().getCreditor().getName())
                        .build();

        BerlinGroupPaymentType paymentType = getPaymentType(paymentRequest);

        CreatePaymentResponse response =
                apiClient.createPayment(
                        createPaymentRequest, paymentType, sessionStorage.get(StorageKeys.STATE));
        PaymentResponse paymentResponse =
                response.toTinkPaymentResponse(paymentRequest.getPayment(), paymentType);

        createdPaymentsList.add(paymentResponse);
        sessionStorage.put(response.getPaymentId(), response.getLinks().getScaRedirect());
        return paymentResponse;
    }
}
