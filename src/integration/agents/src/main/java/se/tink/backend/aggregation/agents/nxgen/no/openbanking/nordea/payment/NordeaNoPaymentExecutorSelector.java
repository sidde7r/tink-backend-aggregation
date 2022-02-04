package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.payment;

import static se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.NordeaNoConstants.ErrorMessages.PAYMENT_NOT_SUPPORTED;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

public class NordeaNoPaymentExecutorSelector implements PaymentExecutor, FetchablePaymentExecutor {

    private final Authenticator nordeaNoAuthenticator;
    private final Credentials credentials;
    private final NordeaNoStandardPaymentExecutor nordeaNoStandardPaymentExecutor;

    public NordeaNoPaymentExecutorSelector(
            NordeaBaseApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            Authenticator nordeaNoAuthenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState) {
        this.nordeaNoAuthenticator = nordeaNoAuthenticator;
        this.credentials = credentials;
        NordeaNoSigningController nordeaNoSigningController = new NordeaNoSigningController(this);
        nordeaNoStandardPaymentExecutor =
                new NordeaNoStandardPaymentExecutor(
                        apiClient,
                        nordeaNoSigningController,
                        supplementalInformationHelper,
                        strongAuthenticationState);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        if (paymentRequest.getPayment().getPaymentServiceType() == PaymentServiceType.PERIODIC) {
            throw new PaymentRejectedException(PAYMENT_NOT_SUPPORTED);
        } else {
            nordeaNoAuthenticator.authenticate(credentials);
            return nordeaNoStandardPaymentExecutor.create(paymentRequest);
        }
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        if (paymentRequest.getPayment().getPaymentServiceType() == PaymentServiceType.PERIODIC) {
            throw new PaymentRejectedException(PAYMENT_NOT_SUPPORTED);
        } else {
            return nordeaNoStandardPaymentExecutor.fetch(paymentRequest);
        }
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        if (paymentMultiStepRequest.getPayment().getPaymentServiceType()
                == PaymentServiceType.PERIODIC) {
            throw new PaymentRejectedException(PAYMENT_NOT_SUPPORTED);
        } else {
            return nordeaNoStandardPaymentExecutor.sign(paymentMultiStepRequest);
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
