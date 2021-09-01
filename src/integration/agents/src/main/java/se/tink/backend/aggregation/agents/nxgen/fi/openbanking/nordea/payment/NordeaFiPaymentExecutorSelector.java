package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.payment;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

public class NordeaFiPaymentExecutorSelector implements PaymentExecutor, FetchablePaymentExecutor {

    private final Authenticator nordeaFiAuthenticator;
    private final Credentials credentials;
    private final NordeaFiRecurringPaymentExecutor nordeaFiRecurringPaymentExecutor;
    private final NordeaFiStandardPaymentExecutor nordeaFiStandardPaymentExecutor;

    public NordeaFiPaymentExecutorSelector(
            NordeaBaseApiClient apiClient,
            SupplementalInformationController supplementalInformationController,
            Authenticator nordeaFiAuthenticator,
            SessionStorage sessionStorage,
            Credentials credentials) {
        this.nordeaFiAuthenticator = nordeaFiAuthenticator;
        this.credentials = credentials;
        NordeaFiSigningController nordeaFiSigningController = new NordeaFiSigningController(this);
        nordeaFiRecurringPaymentExecutor =
                new NordeaFiRecurringPaymentExecutor(
                        apiClient, supplementalInformationController, nordeaFiSigningController);
        nordeaFiStandardPaymentExecutor =
                new NordeaFiStandardPaymentExecutor(
                        apiClient,
                        supplementalInformationController,
                        sessionStorage,
                        nordeaFiSigningController);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        if (paymentRequest.getPayment().getPaymentServiceType() == PaymentServiceType.PERIODIC) {
            nordeaFiAuthenticator.authenticate(credentials);
            return nordeaFiRecurringPaymentExecutor.createRecurringPayment(paymentRequest);
        } else {
            return nordeaFiStandardPaymentExecutor.create(paymentRequest);
        }
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        if (paymentRequest.getPayment().getPaymentServiceType() == PaymentServiceType.PERIODIC) {
            return nordeaFiRecurringPaymentExecutor.fetch(paymentRequest);
        } else {
            return nordeaFiStandardPaymentExecutor.fetch(paymentRequest);
        }
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        if (paymentMultiStepRequest.getPayment().getPaymentServiceType()
                == PaymentServiceType.PERIODIC) {
            return nordeaFiRecurringPaymentExecutor.signRecurring(paymentMultiStepRequest);
        } else {
            return nordeaFiStandardPaymentExecutor.sign(paymentMultiStepRequest);
        }
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
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }
}
