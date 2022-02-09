package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.payment;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.libraries.payment.enums.PaymentStatus;

@Slf4j
@RequiredArgsConstructor
public class NordeaNoSigningController implements Signer<PaymentRequest> {
    private static final int MAX_ATTEMPTS = 90;

    private final NordeaNoPaymentExecutorSelector paymentExecutor;

    public void sign(PaymentRequest toSign) throws AuthenticationException {
        poll(toSign);
    }

    private void poll(PaymentRequest toSign) throws AuthenticationException {
        PaymentStatus status = null;

        for (int i = 0; i < MAX_ATTEMPTS; ++i) {
            status = collect(toSign);

            switch (status) {
                case PAID:
                case SIGNED:
                    return;
                case PENDING:
                    log.info("Waiting for signing");
                    break;
                case REJECTED:
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                case USER_APPROVAL_FAILED:
                    throw ThirdPartyAppError.TIMED_OUT.exception();
                case CANCELLED:
                    throw new PaymentCancelledException();
                default:
                    log.warn(String.format("Unknown payment sign response status: (%s)", status));
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        log.info(String.format("Payment sign timed out internally, last status: %s", status));
        throw ThirdPartyAppError.TIMED_OUT.exception();
    }

    private PaymentStatus collect(PaymentRequest toCollect) throws AuthenticationException {
        try {
            return paymentExecutor.fetch(toCollect).getPayment().getStatus();
        } catch (PaymentException e) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }
    }
}
