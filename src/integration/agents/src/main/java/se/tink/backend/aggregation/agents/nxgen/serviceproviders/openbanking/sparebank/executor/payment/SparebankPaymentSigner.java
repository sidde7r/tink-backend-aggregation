package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
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
    private final StrongAuthenticationState strongAuthenticationState;
    private final SparebankStorage storage;

    public void initiate() {
        openThirdPartyApp(buildAuthorizeUrl());
        waitForSupplementalInformation();
        fetchDebtorAccount();
    }

    private void fetchDebtorAccount() {
        apiClient.fetchAccounts();
    }

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

    private void waitForSupplementalInformation() {
        Optional<Map<String, String>> maybeSupplementalInformation =
                supplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

        maybeSupplementalInformation.map(
                supplementalInformation -> {
                    if (supplementalInfoContainsRequiredFields(supplementalInformation)) {
                        storage.storeSessionData(
                                supplementalInformation.get(
                                        SparebankConstants.StorageKeys.FIELD_PSU_ID),
                                supplementalInformation.get(
                                        SparebankConstants.StorageKeys.FIELD_TPP_SESSION_ID));
                        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
                    } else {
                        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.CANCELLED);
                    }
                });
    }

    private boolean supplementalInfoContainsRequiredFields(
            Map<String, String> supplementalInformation) {
        return supplementalInformation.containsKey(SparebankConstants.StorageKeys.FIELD_PSU_ID)
                && supplementalInformation.containsKey(
                        SparebankConstants.StorageKeys.FIELD_TPP_SESSION_ID);
    }

    private Optional<ThirdPartyAppAuthenticationPayload> getAppPayload(URL authorizeUrl) {
        return Optional.of(ThirdPartyAppAuthenticationPayload.of(authorizeUrl));
    }

    private URL buildAuthorizeUrl() {
        final String state = strongAuthenticationState.getState();
        storage.storeState(state);
        return apiClient
                .getScaRedirect(state)
                .getRedirectUri()
                .filter(StringUtils::isNotBlank)
                .map(URL::new)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SparebankConstants.ErrorMessages.SCA_REDIRECT_MISSING));
    }

    private String getPaymentAuthorizeUrl(String paymentId) {
        return storage.getPaymentUrls(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment authorize Url is missing."))
                .getStartAuthorisation()
                .getHref();
    }
}
