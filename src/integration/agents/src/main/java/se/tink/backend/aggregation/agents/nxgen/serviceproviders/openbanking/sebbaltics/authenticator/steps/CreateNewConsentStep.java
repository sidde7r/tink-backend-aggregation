package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.steps;

import com.google.common.util.concurrent.Uninterruptibles;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.PollValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.ScaAuthMethods;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.SebBalticsDecoupledAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.entities.AccountNumberEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.ConsentAuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.ConsentAuthMethodSelectionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.ConsentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RequiredArgsConstructor
@Slf4j
public class CreateNewConsentStep implements AuthenticationStep {

    private final SebBalticsDecoupledAuthenticator authenticator;
    private final SebBalticsApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final CredentialsRequest credentialsRequest;
    private final LocalDate localDate;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        if (!credentialsRequest.getUserAvailability().isUserAvailableForInteraction()) {
            throw new IllegalStateException("User is not present");
        }

        AccountsResponse accountList = apiClient.fetchAccountsList();

        List<AccountNumberEntity> accountNumberEntity =
                accountList.getAccounts().stream()
                        .map(AccountEntity::getIban)
                        .map(AccountNumberEntity::toAccountNumberEntity)
                        .collect(Collectors.toList());

        AccessEntity accessEntity = new AccessEntity(accountNumberEntity);

        ConsentResponse consentResponse =
                apiClient.createNewConsent(
                        ConsentRequest.builder()
                                .access(accessEntity)
                                .recurringIndicator(true)
                                .validUntil(localDate.plusDays(90).toString())
                                .build());

        String consentId = consentResponse.getConsentId();

        ConsentAuthorizationResponse consentAuthorizationResponse =
                apiClient.startConsentAuthorization(consentId);

        String authorizationId = consentAuthorizationResponse.getAuthorizationId();

        ConsentAuthMethodSelectionResponse response =
                apiClient.updateConsentAuthorization(
                        ConsentAuthMethod.builder()
                                .chosenScaMethod(ScaAuthMethods.SMART_ID)
                                .build(),
                        authorizationId,
                        consentId);

        authenticator.displayChallengeCodeToUser(response.getChallengeData().getCode());

        poll(consentId);

        persistentStorage.put(StorageKeys.USER_CONSENT_ID, consentId);

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    @Override
    public String getIdentifier() {
        return "create_new_consent_step";
    }

    private void poll(String consentId) throws AuthenticationException, AuthorizationException {
        String status = null;

        for (int i = 0; i < PollValues.SMART_ID_POLL_MAX_ATTEMPTS; i++) {
            status = apiClient.getConsentStatus(consentId).getConsentStatus();

            switch (status) {
                case ConsentStatus.VALID:
                    // consent successfully given
                    return;
                case ConsentStatus.RECEIVED:
                    log.info("Consent request initiated");
                    break;
                case ConsentStatus.REJECTED:
                    log.info("Consent rejected");
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
                case ConsentStatus.REVOKED_BY_PSU:
                    log.info("Consent revoked by PSU");
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
                case ConsentStatus.EXPIRED:
                    log.info("Consent expired");
                    throw ThirdPartyAppError.TIMED_OUT.exception();
                case ConsentStatus.TERMINATED_BY_TPP:
                    log.info("Consent terminated by TPP");
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
                default:
                    log.warn(String.format("Unknown status (%s)", status));
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
            }

            Uninterruptibles.sleepUninterruptibly(
                    PollValues.SMART_ID_POLL_FREQUENCY, TimeUnit.MILLISECONDS);
        }

        log.info(String.format("Time out internally, last status: %s", status));
        throw ThirdPartyAppError.TIMED_OUT.exception();
    }
}
