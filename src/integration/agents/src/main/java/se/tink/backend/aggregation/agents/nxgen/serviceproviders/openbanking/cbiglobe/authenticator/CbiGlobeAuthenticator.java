package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class CbiGlobeAuthenticator {

    protected final CbiGlobeApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final CbiGlobeConfiguration configuration;

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

    public URL buildAuthorizeUrl(String state, ConsentRequest consentRequest) {
        ConsentResponse consentResponse = createConsent(consentRequest, state);

        return getScaUrl(consentResponse);
    }

    protected URL getScaUrl(ConsentResponse consentResponse) {
        return new URL(consentResponse.getLinks().getAuthorizeUrl().getHref());
    }

    protected ConsentResponse createConsent(ConsentRequest consentRequest, String state) {
        ConsentResponse consentResponse =
                apiClient.createConsent(consentRequest, createRedirectUrl(state));
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        return consentResponse;
    }

    public ConsentRequest createConsentRequestAccount() {
        return new ConsentRequest(
                new AccessEntity(FormValues.ALL_ACCOUNTS),
                FormValues.TRUE,
                FormValues.FREQUENCY_PER_DAY_ONE,
                FormValues.FALSE,
                generateValidUntilDate());
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
                generateValidUntilDate());
    }

    private String generateValidUntilDate() {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                new DateTime(new Date()).plusDays(15).toDate());
    }

    protected String createRedirectUrl(String state) {
        return new URL(configuration.getRedirectUrl())
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.CODE, QueryValues.CODE)
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
}
