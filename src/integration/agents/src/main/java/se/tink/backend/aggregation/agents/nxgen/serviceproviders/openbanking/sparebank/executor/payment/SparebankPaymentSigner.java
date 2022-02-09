package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

@Slf4j
@RequiredArgsConstructor
public class SparebankPaymentSigner implements Signer<PaymentRequest> {

    private static final int MAX_ATTEMPTS = 90;

    private final SparebankApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SparebankStorage storage;

    public void sign(PaymentRequest toSign) throws AuthenticationException {
        final String paymentId = toSign.getPayment().getUniqueId();
        final String state = strongAuthenticationState.getState();

        storage.storeState(state);
        openThirdPartyApp(
                new URL(
                        apiClient
                                .authorizePayment(getPaymentAuthorizeUrl(paymentId))
                                .getLinks()
                                .getScaRedirect()
                                .getHref()));

        supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);
    }

    @SneakyThrows
    public PaymentResponse poll(PaymentRequest toSign) {
        PaymentStatus paymentStatus = null;
        final String paymentId = toSign.getPayment().getUniqueId();

        for (int i = 0; i < MAX_ATTEMPTS; ++i) {
            paymentStatus = getPaymentStatus(paymentId);
            switch (paymentStatus) {
                case PAID:
                    return getPaymentResponse(toSign, paymentStatus);
                case CREATED:
                    log.info("Waiting for signing");
                    break;
                case REJECTED:
                    throw new PaymentRejectedException("Payment rejected by Bank");
                case CANCELLED:
                    throw new PaymentCancelledException("Payment Cancelled by PSU");
                default:
                    log.warn("Unknown payment sign response status: {}", paymentStatus);
                    throw new PaymentAuthorizationException();
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }
        log.info("Payment sign timed out internally, last status: {}", paymentStatus);
        throw ThirdPartyAppError.TIMED_OUT.exception();
    }

    private PaymentStatus getPaymentStatus(String paymentId) {
        return SparebankPaymentStatus.fromString(
                        apiClient
                                .fetchPaymentStatus(getPaymentStatusUrl(paymentId))
                                .getTransactionStatus())
                .getTinkPaymentStatus();
    }

    private PaymentResponse getPaymentResponse(
            PaymentRequest paymentRequest, PaymentStatus status) {
        final Payment payment = paymentRequest.getPayment();
        return apiClient
                .fetchPayment(getPaymentResponseUrl(payment.getUniqueId()))
                .toTinkPaymentResponse(payment, status);
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

    private String getPaymentStatusUrl(String paymentId) {
        return storage.getPaymentUrls(paymentId)
                .orElseThrow(() -> new IllegalStateException("Empty payment status url."))
                .getStatus()
                .getHref();
    }

    private String getPaymentResponseUrl(String paymentId) {
        return storage.getPaymentUrls(paymentId)
                .orElseThrow(() -> new IllegalStateException("Empty get payment response url."))
                .getSelf()
                .getHref();
    }
}
