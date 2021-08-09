package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_DAILY;
import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_SECONDS_T_WITH_TIMEZONE;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Strings;
import java.text.ParseException;
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
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.TppMessagesEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.AuthenticationMethods;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CredentialsDetailRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CredentialsDetailResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PaymentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.TppErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdateAuthenticationMethodRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdateConsentPsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.utils.authentication.AuthenticationType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

@Slf4j
@AllArgsConstructor
public class ConsentManager {

    private static final long SLEEP_TIME = 3_000L;
    private static final int RETRY_ATTEMPTS = 60;

    private final CbiGlobeApiClient apiClient;
    private final CbiUserState userState;
    private final LocalDateTimeSource localDateTimeSource;
    private final long sleepTime;
    private final int retryAttempts;

    public ConsentManager(
            CbiGlobeApiClient apiClient,
            CbiUserState userState,
            LocalDateTimeSource localDateTimeSource) {
        this(apiClient, userState, localDateTimeSource, SLEEP_TIME, RETRY_ATTEMPTS);
    }

    public ConsentResponse createAllPsd2Consent(String state, AccessType allPsd2) {
        ConsentRequest consentRequestAccount = createConsentRequestAllPsd2(allPsd2);
        return create(state, ConsentType.ACCOUNT, consentRequestAccount, true);
    }

    public ConsentResponse createAccountConsent(String state) {
        ConsentRequest consentRequestAccount = createConsentRequestAccount();
        return create(state, ConsentType.ACCOUNT, consentRequestAccount, false);
    }

    public ConsentResponse createTransactionsConsent(String state) {
        AccountsResponse accountsResponse = userState.getAccountsResponseFromStorage();
        ConsentRequest consentRequestBalancesTransactions =
                createConsentRequestBalancesTransactions(accountsResponse);

        return create(
                state, ConsentType.BALANCE_TRANSACTION, consentRequestBalancesTransactions, false);
    }

    ConsentRequest createConsentRequestAllPsd2(AccessType allPsd2) {
        return new ConsentRequest(
                new AccessEntity(allPsd2),
                FormValues.TRUE,
                FormValues.FREQUENCY_PER_DAY,
                FormValues.TRUE,
                createConsentValidDate());
    }

    ConsentRequest createConsentRequestAccount() {
        return new ConsentRequest(
                new AccessEntity(FormValues.ALL_ACCOUNTS),
                FormValues.TRUE,
                FormValues.FREQUENCY_PER_DAY_ONE,
                FormValues.FALSE,
                createConsentValidDate());
    }

    ConsentRequest createConsentRequestBalancesTransactions(AccountsResponse accountsResponse) {
        List<AccountDetailsEntity> accountDetailsEntities =
                accountsResponse.getAccounts().stream()
                        .map(acc -> new AccountDetailsEntity(acc.getResourceId(), acc.getIban()))
                        .collect(Collectors.toList());

        return new ConsentRequest(
                new AccessEntity(accountDetailsEntities, accountDetailsEntities),
                FormValues.TRUE,
                FormValues.FREQUENCY_PER_DAY,
                FormValues.TRUE,
                createConsentValidDate());
    }

    private String createConsentValidDate() {
        return localDateTimeSource
                .now()
                .toLocalDate()
                .plusDays(FormValues.CONSENT_VALID_PERIOD_DAYS)
                .toString();
    }

    private ConsentResponse create(
            String state,
            ConsentType consentType,
            ConsentRequest consentRequest,
            boolean allPsd2Supported) {
        userState.setAllPsd2Supported(allPsd2Supported);
        ConsentResponse consentResponse =
                apiClient.createConsent(state, consentType, consentRequest);
        userState.startManualAuthenticationStep(consentResponse.getConsentId());

        return consentResponse;
    }

    boolean verifyIfConsentIsAccepted() throws SessionException {
        ConsentStatus consentStatus = retryCallForConsentStatus();

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
        throw e;
    }

    private boolean isConsentsProblem(String message) {
        return message.contains(MessageCodes.CONSENT_INVALID.name())
                || message.contains(MessageCodes.CONSENT_EXPIRED.name())
                || message.contains(MessageCodes.RESOURCE_UNKNOWN.name());
    }

    public ConsentResponse updateAuthenticationMethod() {
        String chosenMethodId = userState.getChosenAuthenticationMethodId();
        return updateAuthenticationMethod(chosenMethodId);
    }

    public ConsentResponse updateAuthenticationMethod(String authenticationMethodId) {
        String consentId = userState.getConsentId();
        UpdateAuthenticationMethodRequest body =
                new UpdateAuthenticationMethodRequest(authenticationMethodId);

        return apiClient.updateConsent(body, Urls.UPDATE_CONSENTS.concat("/" + consentId));
    }

    public <T> T updatePsuCredentials(
            PsuCredentialsResponse psuCredentialsResponse, URL url, Class<T> responseClass) {
        String username = userState.getUsername();
        String password = userState.getPassword();
        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        PsuCredentialsRequest psuCredentials =
                createPsuCredentialsRequest(username, password, psuCredentialsResponse);
        UpdateConsentPsuCredentialsRequest body =
                new UpdateConsentPsuCredentialsRequest(psuCredentials);

        try {
            return apiClient.updatePsuCredentials(url, body, responseClass);
        } catch (HttpResponseException e) {
            TppErrorResponse tppErrorResponse = e.getResponse().getBody(TppErrorResponse.class);
            if (isInvalidCredentials(tppErrorResponse)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            throw e;
        }
    }

    public ConsentResponse changeAuthenticationMethod(
            PaymentAuthorizationResponse paymentAuthorizationResponse, URL url) {
        UpdateAuthenticationMethodRequest updateAuthenticationMethodRequest =
                new UpdateAuthenticationMethodRequest(
                        AuthenticationMethods.getAuthenticationMethodId(
                                paymentAuthorizationResponse.getScaMethods(),
                                AuthenticationType.PUSH_OTP));
        return apiClient.updateConsent(updateAuthenticationMethodRequest, url);
    }

    public String getConsentId() {
        return userState.getConsentId();
    }

    private boolean isInvalidCredentials(TppErrorResponse tppErrorResponse) {
        List<TppMessagesEntity> tppMessages = tppErrorResponse.getTppMessages();
        return CollectionUtils.isNotEmpty(tppMessages)
                && tppMessages.stream()
                        .anyMatch(
                                tppMessagesEntity ->
                                        MessageCodes.PSU_CREDENTIALS_INVALID
                                                .name()
                                                .equals(tppMessagesEntity.getCode()));
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
        ConsentStatus consentStatus = retryCallForConsentStatus();
        if (!consentStatus.isAcceptedStatus()) {
            log.warn("Authorization failed, consents status is not accepted.");
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
        }
    }

    private Retryer<ConsentStatus> getApprovalStatusRetryer() {
        return RetryerBuilder.<ConsentStatus>newBuilder()
                .retryIfResult(
                        consentStatus ->
                                consentStatus == null || consentStatus == ConsentStatus.RECEIVED)
                .withWaitStrategy(WaitStrategies.fixedWait(sleepTime, TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(retryAttempts))
                .build();
    }

    public ConsentStatus retryCallForConsentStatus() {
        Retryer<ConsentStatus> approvalStatusRetryer = getApprovalStatusRetryer();
        try {
            return approvalStatusRetryer.call(
                    () -> apiClient.getConsentStatus(StorageKeys.CONSENT_ID));
        } catch (ExecutionException | RetryException e) {
            if (e.getCause() instanceof HttpResponseException) {
                handleInvalidConsents((HttpResponseException) e.getCause());
            }
            log.warn("Authorization failed, consents status is not accepted.", e);
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(e);
        }
    }

    public void storeConsentValidUntilDateInCredentials() throws SessionException {
        String consentValidUntil =
                apiClient.getConsentDetails(StorageKeys.CONSENT_ID).getValidUntil();
        Date expiryDate =
                parseDate(consentValidUntil, FORMATTER_DAILY, FORMATTER_SECONDS_T_WITH_TIMEZONE);
        userState.storeConsentExpiryDateInCredentials(expiryDate);
    }

    private Date parseDate(String date, ThreadSafeDateFormat... expectedFormats) {
        for (ThreadSafeDateFormat format : expectedFormats) {
            try {
                return format.parse(date);
            } catch (ParseException e) {
                // After all formats fail, we throw exception
            }
        }

        log.error("Could not parse the consent validUntil field to expected formats.");
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
