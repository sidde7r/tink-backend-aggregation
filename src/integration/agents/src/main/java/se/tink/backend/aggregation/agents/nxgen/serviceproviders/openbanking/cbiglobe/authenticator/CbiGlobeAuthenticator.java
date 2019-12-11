package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls.CbiGlobeUtils;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CbiGlobeAuthenticator {

    protected final CbiGlobeApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final CbiGlobeConfiguration configuration;
    private static final Logger logger = LoggerFactory.getLogger(CbiGlobeAuthenticator.class);
    private static final int CONSENT_VALID_PERIOD_DAYS = 89;

    public CbiGlobeAuthenticator(
            CbiGlobeApiClient apiClient,
            PersistentStorage persistentStorage,
            CbiGlobeConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    protected CbiGlobeConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public URL buildAuthorizeUrl(String redirectUrl, ConsentRequest consentRequest) {
        ConsentResponse consentResponse = createConsent(consentRequest, redirectUrl);
        return getScaUrl(consentResponse);
    }

    protected URL getScaUrl(ConsentResponse consentResponse) {
        String url = consentResponse.getLinks().getAuthorizeUrl().getHref();

        return new URL(CbiGlobeUtils.encodeBlankSpaces(url));
    }

    protected ConsentResponse createConsent(ConsentRequest consentRequest, String redirectUrl) {
        ConsentResponse consentResponse = apiClient.createConsent(consentRequest, redirectUrl);
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        return consentResponse;
    }

    public ConsentRequest createConsentRequestAccount() {
        return new ConsentRequest(
                new AccessEntity(FormValues.ALL_ACCOUNTS),
                FormValues.TRUE,
                FormValues.FREQUENCY_PER_DAY_ONE,
                FormValues.FALSE,
                LocalDate.now().plusDays(CONSENT_VALID_PERIOD_DAYS).toString());
    }

    public ConsentRequest createConsentRequestBalancesTransactions(
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
                LocalDate.now().plusDays(CONSENT_VALID_PERIOD_DAYS).toString());
    }

    protected String createRedirectUrl(String state, ConsentType consentType) {
        return new URL(configuration.getRedirectUrl())
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.CODE, consentType.getCode())
                .get();
    }

    public void getToken() {
        final String authorizationHeader =
                "Basic "
                        + Base64.getEncoder()
                                .encodeToString(
                                        (configuration.getClientId()
                                                        + ":"
                                                        + configuration.getClientSecret())
                                                .getBytes());

        GetTokenResponse getTokenResponse = apiClient.getToken(authorizationHeader);
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, getTokenResponse.toTinkToken());
    }

    public void autoAutenthicate() throws SessionException {
        tokenAutoAuthentication();
        consentAutoAuthentication();
    }

    private void consentAutoAuthentication() throws SessionException {
        ConsentStatus consentStatus;
        try {
            consentStatus = getConsentStatus(StorageKeys.CONSENT_ID);
        } catch (HttpResponseException e) {
            handleInvalidConsents(e);
            return;
        }
        if (!consentStatus.isAcceptedStatus()) {
            // only for testing, this commit will be reverted after tests
            logger.info("CONSENT STATUS: " + consentStatus);
            apiClient.removeAccountsFromStorage();
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    public void tokenAutoAuthentication() {
        try {
            if (!apiClient.isTokenValid()) {
                getToken();
            }
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            if (message.contains(MessageCodes.NO_ACCESS_TOKEN_IN_STORAGE.name())) {
                getToken();
            } else {
                throw e;
            }
        }
    }

    public ConsentStatus getConsentStatus(String consentType) throws SessionException {
        String consentStatusString = LogTags.UNKNOWN_STATE;
        try {
            consentStatusString = apiClient.getConsentStatus(consentType).getConsentStatus();
            return ConsentStatus.valueOf(consentStatusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(LogTags.UNKNOWN_STATE + "=" + consentStatusString, e);
        }
    }

    private void handleInvalidConsents(HttpResponseException rethrowIfNotConsentProblems)
            throws SessionException {
        final String message = rethrowIfNotConsentProblems.getResponse().getBody(String.class);
        if (isConsentsProblem(message)) {
            apiClient.removeConsentFromPersistentStorage();
            throw SessionError.SESSION_EXPIRED.exception();
        }
        throw rethrowIfNotConsentProblems;
    }

    private boolean isConsentsProblem(String message) {
        return message.contains(MessageCodes.CONSENT_INVALID.name())
                || message.contains(MessageCodes.CONSENT_EXPIRED.name())
                || message.contains(MessageCodes.RESOURCE_UNKNOWN.name());
    }

    public GetAccountsResponse fetchAccounts() {
        return apiClient.fetchAccounts();
    }
}
