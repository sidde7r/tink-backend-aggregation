package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.CommerzBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.SupplementalInfo;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationStatus;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Slf4j
@RequiredArgsConstructor
public class CommerzBankDecoupledPaymentAuthenticator {

    private static final int DEFAULT_POLL_ATTEMPTS = 40;
    private static final long DEFAULT_DELAY_IN_MILLIS = 5000L;

    private final CommerzBankApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationController controller;
    private final SupplementalInformationFormer supplementalInformationFormer;

    private final int pollAttempts;
    private final long pollDelayInMilliseconds;

    public CommerzBankDecoupledPaymentAuthenticator(
            CommerzBankApiClient apiClient,
            SessionStorage sessionStorage,
            SupplementalInformationController controller,
            SupplementalInformationFormer supplementalInformationFormer) {
        this(
                apiClient,
                sessionStorage,
                controller,
                supplementalInformationFormer,
                DEFAULT_POLL_ATTEMPTS,
                DEFAULT_DELAY_IN_MILLIS);
    }

    public void authenticate() {
        displayMessageAndWait();
        pollForAuthorizationStatus();
    }

    private void pollForAuthorizationStatus() {
        for (int i = 0; i < pollAttempts; i++) {
            AuthorizationStatus scaStatus = getAuthorizationStatus();

            if (scaStatus.isFinalised() || scaStatus.isExempted()) {
                return;
            }

            if (scaStatus.isFailed()) {
                throw ThirdPartyAppError.CANCELLED.exception();
            }

            Uninterruptibles.sleepUninterruptibly(pollDelayInMilliseconds, TimeUnit.MILLISECONDS);
        }
        throw ThirdPartyAppError.TIMED_OUT.exception();
    }

    private AuthorizationStatus getAuthorizationStatus() {
        AuthorizationStatusResponse authorizationStatusResponse =
                apiClient.fetchAuthorizationStatus(sessionStorage.get(StorageKeys.SCA_STATUS_LINK));
        return authorizationStatusResponse.getScaStatus();
    }

    private void displayMessageAndWait() {
        Field field =
                supplementalInformationFormer.getField(SupplementalInfo.PAYMENT_CONFIRMATION_FIELD);
        try {
            controller.askSupplementalInformationSync(field);
        } catch (SupplementalInfoException e) {
            log.info("Supplemental Exception: " + e.getMessage());
        }
    }
}
