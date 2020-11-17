package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_DAILY;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CredentialsDetailRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CredentialsDetailResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdateConsentPsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@AllArgsConstructor
public class ConsentManager {

    private static final long SLEEP_TIME = 3_000L;
    private static final int RETRY_ATTEMPTS = 60;

    private final CbiGlobeApiClient apiClient;
    private final CbiUserState userState;
    private final long sleepTime;
    private final int retryAttempts;

    public ConsentManager(CbiGlobeApiClient apiClient, CbiUserState userState) {
        this(apiClient, userState, SLEEP_TIME, RETRY_ATTEMPTS);
    }

    public ConsentResponse createAccountConsent(String state) {
        ConsentRequest consentRequestAccount = createConsentRequestAccount();
        return create(state, ConsentType.ACCOUNT, consentRequestAccount);
    }

    public ConsentResponse createTransactionsConsent(String state) {
        GetAccountsResponse getAccountsResponse = apiClient.fetchAccounts();
        ConsentRequest consentRequestBalancesTransactions =
                createConsentRequestBalancesTransactions(getAccountsResponse);

        return create(state, ConsentType.BALANCE_TRANSACTION, consentRequestBalancesTransactions);
    }

    ConsentRequest createConsentRequestAccount() {
        return new ConsentRequest(
                new AccessEntity(FormValues.ALL_ACCOUNTS),
                FormValues.TRUE,
                FormValues.FREQUENCY_PER_DAY_ONE,
                FormValues.FALSE,
                LocalDate.now().plusDays(FormValues.CONSENT_VALID_PERIOD_DAYS).toString());
    }

    ConsentRequest createConsentRequestBalancesTransactions(
            GetAccountsResponse getAccountsResponse) {
        List<AccountDetailsEntity> accountDetailsEntities =
                getAccountsResponse.getAccounts().stream()
                        .map(acc -> new AccountDetailsEntity(acc.getResourceId(), acc.getIban()))
                        .collect(Collectors.toList());

        return new ConsentRequest(
                new AccessEntity(accountDetailsEntities, accountDetailsEntities),
                FormValues.TRUE,
                FormValues.FREQUENCY_PER_DAY,
                FormValues.TRUE,
                LocalDate.now().plusDays(FormValues.CONSENT_VALID_PERIOD_DAYS).toString());
    }

    private ConsentResponse create(
            String state, ConsentType consentType, ConsentRequest consentRequest) {
        ConsentResponse consentResponse =
                apiClient.createConsent(state, consentType, consentRequest);
        userState.startManualAuthenticationStep(consentResponse.getConsentId());

        return consentResponse;
    }

    boolean isConsentAccepted() throws SessionException {
        ConsentStatus consentStatus;
        try {
            consentStatus = apiClient.getConsentStatus(StorageKeys.CONSENT_ID);
        } catch (HttpResponseException e) {
            handleInvalidConsents(e);
            throw e;
        }

        if (!consentStatus.isAcceptedStatus()) {
            userState.resetAuthenticationState();
            throw SessionError.SESSION_EXPIRED.exception();
        }

        return true;
    }

    private void handleInvalidConsents(HttpResponseException e) throws SessionException {
        final String message = e.getResponse().getBody(String.class);
        if (isConsentsProblem(message)) {
            userState.resetAuthenticationState();
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private boolean isConsentsProblem(String message) {
        return message.contains(MessageCodes.CONSENT_INVALID.name())
                || message.contains(MessageCodes.CONSENT_EXPIRED.name())
                || message.contains(MessageCodes.RESOURCE_UNKNOWN.name())
                || message.contains(MessageCodes.CONSENT_ALREADY_IN_USE.name());
    }

    public ConsentResponse updateAuthenticationMethod() {
        String chosenMethodId = userState.getChosenAuthenticationMethodId();
        return updateAuthenticationMethod(chosenMethodId);
    }

    public ConsentResponse updateAuthenticationMethod(String authenticationMethodId) {
        String consentId = userState.getConsentId();
        UpdateConsentRequest body = new UpdateConsentRequest(authenticationMethodId);

        return apiClient.updateConsent(consentId, body);
    }

    public ConsentResponse updatePsuCredentials(
            String username, String password, PsuCredentialsResponse psuCredentialsResponse) {
        String consentId = userState.getConsentId();
        PsuCredentialsRequest psuCredentials =
                createPsuCredentialsRequest(username, password, psuCredentialsResponse);
        UpdateConsentPsuCredentialsRequest body =
                new UpdateConsentPsuCredentialsRequest(psuCredentials);

        return apiClient.updateConsentPsuCredentials(consentId, body);
    }

    private PsuCredentialsRequest createPsuCredentialsRequest(
            String username, String password, PsuCredentialsResponse psuCredentialsResponse) {
        List<CredentialsDetailResponse> credentialsDetails =
                Optional.ofNullable(psuCredentialsResponse)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Psu credentials must not be null"))
                        .getCredentialsDetails();

        String usernameCredentialsId =
                findCredentialsIdByPredicate(
                        credentialsDetails,
                        credentialsDetailResponse -> !credentialsDetailResponse.isSecret());

        String passwordCredentialsId =
                findCredentialsIdByPredicate(
                        credentialsDetails, CredentialsDetailResponse::isSecret);

        return new PsuCredentialsRequest(
                psuCredentialsResponse.getAspspProductCode(),
                Arrays.asList(
                        new CredentialsDetailRequest(usernameCredentialsId, username),
                        new CredentialsDetailRequest(passwordCredentialsId, password)));
    }

    private String findCredentialsIdByPredicate(
            List<CredentialsDetailResponse> credentialsDetails,
            Predicate<CredentialsDetailResponse> predicate) {
        return credentialsDetails.stream()
                .filter(predicate)
                .findAny()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "No credentials detail matching predicate"))
                .getCredentialDetailId();
    }

    public void waitForAcceptance() throws LoginException {
        Retryer<ConsentStatus> approvalStatusRetryer = getApprovalStatusRetryer();

        try {
            ConsentStatus consentStatus =
                    approvalStatusRetryer.call(
                            () -> apiClient.getConsentStatus(StorageKeys.CONSENT_ID));
            if (!consentStatus.isAcceptedStatus()) {
                log.warn("Authorization failed, consents status is not accepted.");
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            }
        } catch (ExecutionException | RetryException e) {
            log.warn("Authorization failed, consents status is not accepted.", e);
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(e);
        }

        userState.finishManualAuthenticationStep();
    }

    private Retryer<ConsentStatus> getApprovalStatusRetryer() {
        return RetryerBuilder.<ConsentStatus>newBuilder()
                .retryIfResult(
                        consentStatus -> consentStatus == null || !consentStatus.isFinalStatus())
                .withWaitStrategy(WaitStrategies.fixedWait(sleepTime, TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(retryAttempts))
                .build();
    }

    public void storeConsentValidUntilDateInCredentials() throws SessionException {
        Date expiryDate = null;
        try {
            expiryDate =
                    FORMATTER_DAILY.parse(
                            apiClient.getConsentDetails(StorageKeys.CONSENT_ID).getValidUntil());
        } catch (ParseException e) {
            log.error("Could not parse the consent validUntil field to expected format.");
            throw SessionError.SESSION_EXPIRED.exception();
        }

        userState.storeConsentExpiryDateInCredentials(expiryDate);
    }
}
