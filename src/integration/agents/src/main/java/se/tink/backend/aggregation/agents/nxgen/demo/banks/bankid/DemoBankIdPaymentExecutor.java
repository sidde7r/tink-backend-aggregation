package se.tink.backend.aggregation.agents.nxgen.demo.banks.bankid;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigningController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public class DemoBankIdPaymentExecutor implements PaymentExecutor {

    private final SupplementalRequester supplementalRequester;

    public DemoBankIdPaymentExecutor(SupplementalRequester supplementalRequester) {
        this.supplementalRequester = supplementalRequester;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        return PaymentResponse.of(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException {

        String nextStep;
        final Payment payment = paymentMultiStepRequest.getPayment();
        PaymentStatus status;

        switch (paymentMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                status = PaymentStatus.CREATED;
                nextStep = SigningStepConstants.STEP_SIGN;
                break;
            case SigningStepConstants.STEP_SIGN:
                // trigger fake bankid QR page
                getSigner().sign(paymentMultiStepRequest);
                nextStep = SigningStepConstants.STEP_FINALIZE;
                status = PaymentStatus.SIGNED;
                break;
            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }
        payment.setStatus(status);
        return new PaymentMultiStepResponse(payment, nextStep, new ArrayList<>());
    }

    private Signer getSigner() {
        return new BankIdSigningController(supplementalRequester, new DemoBankIdSigner());
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
}
