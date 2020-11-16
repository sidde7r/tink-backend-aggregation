package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.configuration.KbcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgentPlatformStorageApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.BerlinGroupAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class KbcApiClient extends BerlinGroupAgentPlatformStorageApiClient<KbcConfiguration> {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Pattern IBAN_PATTERN = Pattern.compile("BE[0-9]{14}");

    private final Credentials credentials;
    private KbcAuthenticationData authData;

    public KbcApiClient(
            final TinkHttpClient client,
            final KbcConfiguration configuration,
            final CredentialsRequest request,
            final String redirectUrl,
            final Credentials credentials,
            final PersistentStorage persistentStorage,
            final String qSealc) {
        super(client, persistentStorage, configuration, request, redirectUrl, qSealc);
        this.credentials = credentials;
    }

    @Override
    public BerlinGroupAccountResponse fetchAccounts() {
        return getAccountsRequestBuilder(getConfiguration().getBaseUrl() + Urls.ACCOUNTS)
                .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(Psd2Headers.Keys.PSU_IP_ADDRESS, getConfiguration().getPsuIpAddress())
                .get(AccountResponse.class);
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(String url) {
        logger.info("Fetching transactions endpoint requested: {}", url);
        return getTransactionsRequestBuilder(url)
                .header(BerlinGroupConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .get(TransactionsKeyPaginatorBaseResponse.class);
    }

    @Override
    public RequestBuilder getTransactionsRequestBuilder(final String url) {
        return client.request(url)
                .addBearerToken(getTokenFromSession(StorageKeys.OAUTH_TOKEN))
                .header(
                        BerlinGroupConstants.HeaderKeys.PSU_IP_ADDRESS,
                        getConfiguration().getPsuIpAddress())
                .header(
                        BerlinGroupConstants.HeaderKeys.CONSENT_ID,
                        getKbcPersistedData().getConsentId());
    }

    public URL getAuthorizeUrl(final String state) {
        final String consentId = rotateConsentId();
        final String authUrl = Urls.BASE_AUTH_URL + Urls.AUTH;
        return getAuthorizeUrlWithCode(
                        authUrl,
                        state,
                        consentId,
                        getConfiguration().getClientId(),
                        getRedirectUrl())
                .getUrl();
    }

    @Override
    public OAuth2Token getToken(final String code) {
        final TokenRequest tokenRequest =
                new TokenRequest(
                        FormValues.AUTHORIZATION_CODE,
                        code,
                        getRedirectUrl(),
                        getConfiguration().getClientId(),
                        getKbcPersistedData().getCodeVerifier());

        return client.request(getConfiguration().getBaseUrl() + Urls.TOKEN)
                .addBasicAuth(getConfiguration().getClientId())
                .body(tokenRequest.toData(), MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                        BerlinGroupConstants.HeaderKeys.PSU_IP_ADDRESS,
                        getConfiguration().getPsuIpAddress())
                .post(TokenBaseResponse.class)
                .toTinkToken();
    }

    @Override
    public OAuth2Token refreshToken(final String token) {
        rotateConsentId();
        final RefreshTokenRequest refreshTokenRequest =
                new RefreshTokenRequest(
                        FormValues.REFRESH_TOKEN_GRANT_TYPE,
                        token,
                        getConfiguration().getClientId());

        return client.request(getConfiguration().getBaseUrl() + Urls.TOKEN)
                .addBasicAuth(getConfiguration().getClientId())
                .header(
                        BerlinGroupConstants.HeaderKeys.PSU_IP_ADDRESS,
                        getConfiguration().getPsuIpAddress())
                .body(refreshTokenRequest.toData(), MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenBaseResponse.class)
                .toTinkToken();
    }

    private String rotateConsentId() {
        try {
            final String consentId = getConsentId();
            persistentStorage.put(StorageKeys.CONSENT_ID, consentId);
            return consentId;
        } catch (HttpResponseException exception) {
            logger.error(exception.getMessage());
            if (checkResponseExceptionForIbanErrors(exception.getMessage())) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(exception);
            } else {
                throw exception;
            }
        }
    }

    private boolean checkResponseExceptionForIbanErrors(String exceptionMessage) {
        return exceptionMessage.contains("CONSENT_INVALID")
                || exceptionMessage.contains("FORMAT_ERROR");
    }

    @Override
    public String getConsentId() {
        final String iban = credentials.getField(KbcConstants.CredentialKeys.IBAN);

        validateIban(iban);

        final List<String> ibanList = Collections.singletonList(iban);

        final AccessEntity accessEntity =
                new AccessEntity.Builder()
                        .withBalances(ibanList)
                        .withTransactions(ibanList)
                        .build();

        final ConsentBaseRequest consentsRequest = new ConsentBaseRequest();
        consentsRequest.setAccess(accessEntity);

        return client.request(getConfiguration().getBaseUrl() + Urls.CONSENT)
                .body(consentsRequest.toData(), MediaType.APPLICATION_JSON_TYPE)
                .header(BerlinGroupConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(BerlinGroupConstants.HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                .header(
                        BerlinGroupConstants.HeaderKeys.PSU_IP_ADDRESS,
                        getConfiguration().getPsuIpAddress())
                .post(ConsentBaseResponse.class)
                .getConsentId();
    }

    private static void validateIban(String iban) {
        if (Objects.isNull(iban) || !IBAN_PATTERN.matcher(iban).matches()) {
            throw new IllegalArgumentException("Iban has incorrect format.");
        }
    }

    @Override
    protected String getConsentIdFromStorage() {
        return getKbcPersistedData().getConsentId();
    }

    private KbcAuthenticationData getKbcPersistedData() {

        if (authData == null) {
            authData =
                    new KbcPersistedDataAccessorFactory(new ObjectMapperFactory().getInstance())
                            .createKbcAuthenticationPersistedDataAccessor(
                                    new PersistentStorageService(persistentStorage)
                                            .readFromAgentPersistentStorage())
                            .getKbcAuthenticationData();
        }
        return authData;
    }
}
