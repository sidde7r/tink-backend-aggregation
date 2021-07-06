package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.steps;

import com.google.common.util.concurrent.Uninterruptibles;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.entities.AccountNumberEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.ConsentAuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.ConsentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class CreateNewConsentStep implements AuthenticationStep {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SebBalticsBaseApiClient apiClient;
    private final PersistentStorage persistentStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        AccountsResponse accountList = apiClient.fetchAccountsList();

        List<AccountNumberEntity> accountNumberEntity =
                accountList.getAccounts().stream()
                        .map(AccountEntity::getIban)
                        .map(AccountNumberEntity::toAccountNumberEntity)
                        .collect(Collectors.toList());

        AccessEntity accessEntity = new AccessEntity();
        accessEntity.setAccounts(accountNumberEntity);
        accessEntity.setBalances(accountNumberEntity);
        accessEntity.setTransactions(accountNumberEntity);

        ConsentResponse consentResponse =
                apiClient.createNewConsent(
                        ConsentRequest.builder()
                                .access(accessEntity)
                                .recurringIndicator(true)
                                .validUntil("2021-07-31")
                                .build());

        String consentId = consentResponse.getConsentId();

        ConsentAuthorizationResponse consentAuthorizationResponse =
                apiClient.startConsentAuthorization(consentId);

        String authorizationId = consentAuthorizationResponse.getAuthorizationId();

        apiClient.updateConsentAuthorization(
                ConsentAuthMethod.builder().chosenScaMethod("SmartID").build(),
                authorizationId,
                consentId);

        poll(consentId);

        persistentStorage.put("USER_CONSENT_ID", consentId);

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private void poll(String consentId) throws AuthenticationException, AuthorizationException {
        String status = null;

        for (int i = 0; i < 90; i++) {
            status = apiClient.getConsentStatus(consentId).getConsentStatus();

            switch (status) {
                case "valid":
                    // consent successfully given
                    return;
                case "received":
                    logger.info("Consent request initiated");
                    break;
                case "rejected":
                    logger.info("Consent rejected");
                    throw new ThirdPartyAppException(ThirdPartyAppError.AUTHENTICATION_ERROR);
                case "revokedByPsu":
                    logger.info("Consent revoked by PSU");
                    throw new ThirdPartyAppException(ThirdPartyAppError.AUTHENTICATION_ERROR);
                case "expired":
                    logger.info("Consent expired");
                    throw new ThirdPartyAppException(ThirdPartyAppError.AUTHENTICATION_ERROR);
                case "terminatedByTpp":
                    logger.info("Consent terminated by TPP");
                    throw new ThirdPartyAppException(ThirdPartyAppError.AUTHENTICATION_ERROR);
                default:
                    logger.warn(String.format("Unknown status (%s)", status));
                    throw new ThirdPartyAppException(ThirdPartyAppError.AUTHENTICATION_ERROR);
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        logger.info(String.format("Time out internally, last status: %s", status));
    }
}
