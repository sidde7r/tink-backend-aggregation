package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.enums.PaymentStatus;

@Slf4j
@RequiredArgsConstructor
public class SparebankPaymentSigner {

    private static final int MAX_ATTEMPTS = 100;

    private final SparebankPaymentExecutor paymentExecutor;
    private final SparebankApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SparebankStorage storage;

    public void sign(PaymentRequest toSign) throws AuthenticationException {
        final String paymentId = toSign.getPayment().getUniqueId();

        openThirdPartyApp(
                new URL(
                        apiClient
                                .authorizePayment(getPaymentAuthorizeUrl(paymentId))
                                .getLinks()
                                .getScaRedirect()
                                .getHref()));
        poll(toSign);
    }

    @SneakyThrows
    private PaymentResponse poll(PaymentRequest toSign) {
        PaymentStatus paymentStatus = null;

        for (int i = 0; i < MAX_ATTEMPTS; ++i) {
            PaymentResponse paymentResponse = collect(toSign);
            paymentStatus = paymentResponse.getPayment().getStatus();
            switch (paymentStatus) {
                case SIGNED:
                case PAID:
                    return paymentResponse;
                case CREATED:
                    log.info("Waiting for signing");
                    Thread.sleep(2000);
                    continue;
                case REJECTED:
                    throw new PaymentRejectedException("Payment rejected by Bank");
                case CANCELLED:
                    throw new PaymentCancelledException("Payment Cancelled by PSU");
                default:
                    log.warn("Unknown payment sign response status: {}", paymentStatus);
                    throw new PaymentAuthorizationException();
            }
        }
        log.info("Payment sign timed out internally, last status: {}", paymentStatus);
        throw ThirdPartyAppError.TIMED_OUT.exception();
    }

    private PaymentResponse collect(PaymentRequest toSign) {
        try {
            return paymentExecutor.fetch(toSign);
        } catch (PaymentException e) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }
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
        return Optional.of(ThirdPartyAppAuthenticationPayload.of(authorizeUrl));
    }

    private String getPaymentAuthorizeUrl(String paymentId) {
        return storage.getPaymentUrls(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment authorize Url is missing."))
                .getStartAuthorisation()
                .getHref();
    }
}
