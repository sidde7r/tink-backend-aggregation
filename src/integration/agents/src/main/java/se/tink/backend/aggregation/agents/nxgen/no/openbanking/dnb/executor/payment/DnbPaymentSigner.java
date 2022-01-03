package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentStatus;

@Slf4j
@RequiredArgsConstructor
public class DnbPaymentSigner implements Signer<PaymentRequest> {
    private static final int MAX_ATTEMPTS = 90;

    private final DnbPaymentExecutor paymentExecutor;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public void sign(PaymentRequest toSign) throws AuthenticationException {

        String id = toSign.getPayment().getUniqueId();
        URL authorizeUrl = new URL(sessionStorage.get(id));

        openThirdPartyApp(authorizeUrl);

        poll(toSign);
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload =
                getAppPayload(authorizeUrl)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Can't translate authorizeUrl to App Payload "
                                                        + authorizeUrl));
        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private Optional<ThirdPartyAppAuthenticationPayload> getAppPayload(URL authorizeUrl) {
        return Optional.ofNullable(authorizeUrl).map(ThirdPartyAppAuthenticationPayload::of);
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
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
                case USER_APPROVAL_FAILED:
                    throw ThirdPartyAppError.TIMED_OUT.exception();
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
