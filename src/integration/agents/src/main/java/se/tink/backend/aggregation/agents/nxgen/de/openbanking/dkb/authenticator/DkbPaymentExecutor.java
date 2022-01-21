package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentExecutor;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentApiClient;
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
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payments.common.model.PaymentScheme;

@Slf4j
public class DkbPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final BasePaymentExecutor basePaymentExecutor;
    private final PaymentAuthenticatorPreAuth paymentAuthenticatorPreAuth;

    public DkbPaymentExecutor(
            PaymentApiClient paymentApiClient,
            PaymentAuthenticatorPreAuth paymentAuthenticatorPreAuth,
            SessionStorage sessionStorage) {
        basePaymentExecutor =
                new BasePaymentExecutor(
                        paymentApiClient, paymentAuthenticatorPreAuth, sessionStorage);
        this.paymentAuthenticatorPreAuth = paymentAuthenticatorPreAuth;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        // Temporary solution to be fixed in NZG-1112
        if (PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER
                == paymentRequest.getPayment().getPaymentScheme()) {
            throw new PaymentValidationException("Instant payment is not supported");
        }
        paymentAuthenticatorPreAuth.preAuthentication();
        return basePaymentExecutor.create(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        return basePaymentExecutor.sign(paymentMultiStepRequest);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return basePaymentExecutor.fetch(paymentRequest);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        return basePaymentExecutor.createBeneficiary(createBeneficiaryMultiStepRequest);
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        return basePaymentExecutor.cancel(paymentRequest);
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return basePaymentExecutor.fetchMultiple(paymentListRequest);
    }
}
