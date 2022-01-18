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

    private static final int MAX_POLL_ATTEMPTS = 40;

    private final CommerzBankApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationController controller;
    private final SupplementalInformationFormer supplementalInformationFormer;

    public void authenticate() {
        displayMessageAndWait();
        pollForAuthorizationStatus();
    }

    private void pollForAuthorizationStatus() {
        for (int i = 0; i < MAX_POLL_ATTEMPTS; i++) {
            AuthorizationStatus scaStatus = getAuthorizationStatus();

            if (scaStatus.isFinalised() || scaStatus.isExempted()) {
                return;
            }

            if (scaStatus.isFailed()) {
                throw ThirdPartyAppError.CANCELLED.exception();
            }

            Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);
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
