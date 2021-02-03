package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment;

import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.AmountEntity;
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
import se.tink.libraries.payment.rpc.Payment;

public class Xs2aDevelopersPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    protected final Xs2aDevelopersApiClient apiClient;
    protected final ThirdPartyAppAuthenticationController controller;
    protected final Credentials credentials;
    protected final PersistentStorage persistentStorage;

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
                                payment.getCurrency(),
                                payment.getExactCurrencyAmount().getExactValue()),
                        payment.getRemittanceInformation().getValue());

        CreatePaymentResponse createPaymentResponse = apiClient.createPayment(createPaymentRequest);
        persistentStorage.put(StorageKeys.PAYMENT_ID, createPaymentResponse.getPaymentId());

        return createPaymentResponse.toTinkPayment();
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        final String paymentId = payment.getUniqueId();
        return apiClient.getPayment(paymentId).toTinkPayment(paymentId);
    }

    public void sign() {
        controller.authenticate(credentials);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
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
