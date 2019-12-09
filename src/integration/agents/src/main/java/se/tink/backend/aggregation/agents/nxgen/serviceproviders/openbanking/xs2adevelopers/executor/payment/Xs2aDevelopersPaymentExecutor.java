package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment;

import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AmountEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Payment;

public class Xs2aDevelopersPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private final Xs2aDevelopersApiClient apiClient;
    private final ThirdPartyAppAuthenticationController controller;
    private final Credentials credentials;
    private final PersistentStorage persistentStorage;

    public Xs2aDevelopersPaymentExecutor(
            Xs2aDevelopersApiClient apiClient,
            ThirdPartyAppAuthenticationController controller,
            Credentials credentials,
            PersistentStorage persistentStorage) {
        this.controller = controller;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        AccountEntity creditor = new AccountEntity(payment.getCreditor().getAccountNumber());
        AccountEntity debtor = new AccountEntity(payment.getDebtor().getAccountNumber());

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest(
                        creditor,
                        payment.getCreditor().getName(),
                        debtor,
                        new AmountEntity(
                                ExactCurrencyAmount.of(
                                        payment.getAmount().toBigDecimal(),
                                        payment.getCurrency())));

        CreatePaymentResponse createPaymentResponse = apiClient.createPayment(createPaymentRequest);
        persistentStorage.put(StorageKeys.PAYMENT_ID, createPaymentResponse.getPaymentId());
        persistentStorage.put(
                StorageKeys.AUTHORISATION_URL, createPaymentResponse.getLinks().getScaOAuth());

        return createPaymentResponse.toTinkPayment(
                creditor, debtor, payment.getExecutionDate(), payment.getAmount());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        final String paymentId = payment.getUniqueId();
        sign();

        return apiClient.getPayment(paymentId).toTinkPayment(paymentId);
    }

    private void sign() {
        try {
            controller.authenticate(credentials);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        return new PaymentMultiStepResponse(
                paymentMultiStepRequest.getPayment(), SigningStepConstants.STEP_FINALIZE, null);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return new PaymentListResponse(
                paymentListRequest.getPaymentRequestList().stream()
                        .map(paymentRequest -> new PaymentResponse(paymentRequest.getPayment()))
                        .collect(Collectors.toList()));
    }
}
