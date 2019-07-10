package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.VolksbankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.VolksbankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.VolksbankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.VolksbankConstants.UrlParameters;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.VolksbankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.entity.ConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.entity.DetailedConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.entity.InitialConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.configuration.VolksbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryValues;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class VolksbankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private VolksbankConfiguration configuration;

    private AccountsResponse accountsResponse = null;

    public VolksbankApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, Credentials credentials) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    private VolksbankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(VolksbankConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(
                        VolksbankConstants.HeaderKeys.X_REQUEST_ID,
                        java.util.UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID));
    }

    public URL buildAuthorizeUrl(String state) {

        ConsentRequest consentRequest =
                getConsentRequest(
                        new InitialConsentAccessEntity(VolksbankConstants.FormValues.ALL_ACCOUNTS));

        ConsentResponse consentResponse = getConsentResponse(state, consentRequest);
        persistentStorage.put(
                VolksbankConstants.StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        return new URL(consentResponse.getLinks().getScaRedirect());
    }

    public ConsentStatusResponse getConsentStatus() {

        return createRequest(
                        Urls.CONSENT_STATUS.parameter(
                                UrlParameters.CONSENT_ID,
                                persistentStorage.get(StorageKeys.CONSENT_ID)))
                .get(ConsentStatusResponse.class);
    }

    public AccountsResponse fetchAccounts() {

        return Objects.isNull(accountsResponse)
                ? accountsResponse =
                        createRequestInSession(Urls.ACCOUNTS).get(AccountsResponse.class)
                : accountsResponse;
    }

    public BalanceResponse fetchAccountBalance(String accountId) {

        return createRequestInSession(
                        Urls.BALANCES.parameter(
                                VolksbankConstants.UrlParameters.ACCOUNT_ID, accountId))
                .get(BalanceResponse.class);
    }

    public ConsentResponse getDetailedConsent(String state) {

        List<String> ibans =
                fetchAccounts().getAccounts().stream()
                        .map(AccountEntity::getIban)
                        .collect(Collectors.toList());

        ConsentRequest consentRequest = getConsentRequest(new DetailedConsentAccessEntity(ibans));
        ConsentResponse consentResponse = getConsentResponse(state, consentRequest);
        persistentStorage.put(
                VolksbankConstants.StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        return consentResponse;
    }

    public ConsentResponse getConsentResponse(String state, ConsentRequest consentRequest) {
        return createRequest(Urls.CONSENTS)
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(getConfiguration().getRedirectUrl())
                                .queryParam(VolksbankConstants.QueryKeys.STATE, state)
                                .queryParam(VolksbankConstants.QueryKeys.CODE, VolksbankConstants.QueryValues.CODE)) // TODO patch server
                .header(HeaderKeys.PSU_ID, credentials.getField(Key.LOGIN_INPUT))
                .header(HeaderKeys.PSU_ID_TYPE, credentials.getField(Key.LOGIN_DESCRIPTION))
                .post(ConsentResponse.class, consentRequest);
    }

    public ConsentRequest getConsentRequest(ConsentAccessEntity consentAccessEntity) {
        return new ConsentRequest(
                true, VolksbankConstants.FormValues.MAX_DATE, 100, false, consentAccessEntity);
    }

    public PaginatorResponse fetchTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return createRequestInSession(
                        Urls.TRANSACTIONS.parameter(
                                UrlParameters.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .queryParam(
                        VolksbankConstants.QueryKeys.DATE_FROM,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(
                        VolksbankConstants.QueryKeys.DATE_TO,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(TransactionsResponse.class);
    }
}
