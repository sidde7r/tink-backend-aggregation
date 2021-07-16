package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosConstants.PathParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.configuration.TriodosConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.fetcher.transactions.entities.TriodosTransactionsKeyPaginatorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountEntityBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponseBerlinGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class TriodosApiClient extends BerlinGroupApiClient<TriodosConfiguration> {
    private final String clientId;
    private final String qSealc;
    private final boolean isUserPresent;
    private final Credentials credentials;
    private final QsealcSigner qsealcSigner;

    TriodosApiClient(
            final TinkHttpClient client,
            final PersistentStorage persistentStorage,
            final TriodosConfiguration configuration,
            final CredentialsRequest request,
            final String redirectUrl,
            final QsealcSigner qsealcSigner,
            final String qSealc) {
        super(client, persistentStorage, configuration, request, redirectUrl, qSealc);
        this.qSealc = qSealc;
        this.isUserPresent = request.getUserAvailability().isUserPresent();
        this.credentials = request.getCredentials();
        this.qsealcSigner = qsealcSigner;
        try {
            this.clientId = CertificateUtils.getOrganizationIdentifier(qSealc);
        } catch (CertificateException e) {
            throw new IllegalStateException("Could not get organization identifier from QsealC", e);
        }
    }

    @Override
    public AccountsBaseResponseBerlinGroup fetchAccounts() {
        final String digest = Psd2Headers.calculateDigest(FormValues.EMPTY);
        final URL accountsUrl = new URL(TriodosConstants.BASE_URL + Urls.ACCOUNTS);
        AccountsBaseResponseBerlinGroup res =
                createRequestInSession(accountsUrl, digest)
                        .get(AccountsBaseResponseBerlinGroup.class);

        final List<AccountEntityBaseEntity> accountsWithBalances =
                res.getAccounts().stream().map(this::fetchBalances).collect(Collectors.toList());

        return new AccountsBaseResponseBerlinGroup(accountsWithBalances);
    }

    public URL getAuthorizeUrl(final String state) {
        final String consentId = getConsentId();
        final String codeVerifier = Psd2Headers.generateCodeVerifier();
        persistentStorage.put(BerlinGroupConstants.StorageKeys.CODE_VERIFIER, codeVerifier);
        final String codeChallenge = Psd2Headers.generateCodeChallenge(codeVerifier);
        persistentStorage.put(BerlinGroupConstants.StorageKeys.CONSENT_ID, consentId);
        final String authUrl = TriodosConstants.AUTH_BASE_URL + Urls.AUTH;

        return getAuthorizeUrl(authUrl, state, clientId, getRedirectUrl())
                .queryParam(QueryKeys.SCOPE, TriodosConstants.QueryValues.SCOPE + consentId)
                .queryParam(QueryKeys.CODE_CHALLENGE, codeChallenge)
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .getUrl();
    }

    private AccountEntityBaseEntity fetchBalances(final AccountEntityBaseEntity accountBaseEntity) {
        final String digest = Psd2Headers.calculateDigest(FormValues.EMPTY);
        final URL url =
                new URL(
                        TriodosConstants.BASE_URL
                                + Urls.AIS_BASE
                                + accountBaseEntity.getBalancesLink());
        final List<BalanceBaseEntity> balances =
                createRequestInSession(url, digest)
                        .get(AccountEntityBaseEntity.class)
                        .getBalances();
        accountBaseEntity.setBalances(balances);

        return accountBaseEntity;
    }

    public TriodosTransactionsKeyPaginatorResponse fetchTransactionsTriodos(String url) {
        final String digest = Psd2Headers.calculateDigest(FormValues.EMPTY);
        final URL fullUrl = new URL(TriodosConstants.BASE_URL + Urls.AIS_BASE + url);

        return createRequestInSession(fullUrl, digest)
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .queryParam(
                        TriodosConstants.QueryKeys.DATE_FROM,
                        TriodosConstants.QueryValues.DATE_FROM)
                .get(TriodosTransactionsKeyPaginatorResponse.class);
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(final String url) {
        throw new UnsupportedOperationException(
                "This method should not be called - call fetchTransactionsTriodos instead");
    }

    @Override
    public OAuth2Token getToken(final String code) {
        final String codeVerifier =
                persistentStorage.get(BerlinGroupConstants.StorageKeys.CODE_VERIFIER);
        final String body =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.CLIENT_CREDENTIALS)
                        .put(FormKeys.REDIRECT_URI, getRedirectUrl())
                        .put(FormKeys.CODE_VERIFIER, codeVerifier)
                        .put(FormKeys.CODE, code)
                        .build()
                        .serialize();
        final TokenBaseResponse token =
                client.request(TriodosConstants.BASE_URL + Urls.TOKEN)
                        .addBasicAuth(clientId, getConfiguration().getClientSecret())
                        .body(body)
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .post(TokenBaseResponse.class);
        setTokenToSession(token.toTinkToken(), BerlinGroupConstants.StorageKeys.OAUTH_TOKEN);
        authorizeConsent();

        return token.toTinkToken();
    }

    @Override
    public String getConsentId() {
        final AccessEntity accessEntity =
                new AccessEntity.Builder()
                        .addIbans(
                                Lists.newArrayList(
                                        Splitter.on(",")
                                                .split(credentials.getField(CredentialKeys.IBANS))))
                        .build();
        final ConsentBaseRequest consentsRequest = new ConsentBaseRequest();
        consentsRequest.setAccess(accessEntity);

        final String digest = Psd2Headers.calculateDigest(consentsRequest.toData());
        if (StringUtils.isNotEmpty(
                persistentStorage.get(BerlinGroupConstants.StorageKeys.CONSENT_ID))) {
            return persistentStorage.get(BerlinGroupConstants.StorageKeys.CONSENT_ID);
        }

        final URL url = new URL(TriodosConstants.BASE_URL + Urls.CONSENT);

        final ConsentResponse consentResponse =
                createRequest(url, digest)
                        .body(consentsRequest.toData())
                        .header(HeaderKeys.PSU_IP_ADDRESS, TriodosConstants.PSU_IPADDRESS)
                        .post(ConsentResponse.class);

        persistentStorage.put(
                BerlinGroupConstants.StorageKeys.CONSENT_ID, consentResponse.getConsentId());
        persistentStorage.put(
                TriodosConstants.HeaderKeys.AUTHORIZATION_ID, consentResponse.getAuthorisationId());

        return consentResponse.getConsentId();
    }

    public ConsentStatusResponse getConsentStatus(final String consentId) {
        final URL url =
                new URL(Urls.CONSENT_STATUS).parameter(PathParameterKeys.CONSENT_ID, consentId);

        return client.request(url)
                .header(HeaderKeys.CONSENT_ID, consentId)
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .get(ConsentStatusResponse.class);
    }

    private void authorizeConsent() {
        final URL url =
                new URL(
                        TriodosConstants.BASE_URL
                                + String.format(
                                        Urls.AUTHORIZE_CONSENT,
                                        persistentStorage.get(
                                                BerlinGroupConstants.StorageKeys.CONSENT_ID),
                                        persistentStorage.get(StorageKeys.AUTHORIZATION_ID)));
        createRequestInSession(url, FormValues.EMPTY).put(String.class);
    }

    @Override
    public OAuth2Token refreshToken(final String token) {
        rotateConsentId();
        final String codeVerifier =
                persistentStorage.get(BerlinGroupConstants.StorageKeys.CODE_VERIFIER);

        final String body =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.REFRESH_TOKEN_GRANT_TYPE)
                        .put(FormKeys.REDIRECT_URI, getRedirectUrl())
                        .put(FormKeys.CODE_VERIFIER, codeVerifier)
                        .put(FormKeys.CODE, token)
                        .put(FormKeys.REFRESH_TOKEN, token)
                        .build()
                        .serialize();

        return client.request(TriodosConstants.BASE_URL + Urls.TOKEN)
                .addBasicAuth(clientId, getConfiguration().getClientSecret())
                .header(
                        BerlinGroupConstants.HeaderKeys.PSU_IP_ADDRESS,
                        TriodosConstants.PSU_IPADDRESS)
                .body(body, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenBaseResponse.class)
                .toTinkToken();
    }

    private String rotateConsentId() {
        final String consentId = getConsentId();
        persistentStorage.put(BerlinGroupConstants.StorageKeys.CONSENT_ID, consentId);
        return consentId;
    }

    private String getAuthorization(final String digest, final String xRequestId) {
        final String certificateKeyId = Psd2Headers.getTppCertificateKeyId(getX509Certificate());

        return new AuthorizationEntity(certificateKeyId, getSignature(digest, xRequestId))
                .toString();
    }

    private String getSignature(final String digest, final String xRequestId) {
        final SignatureEntity signatureEntity = new SignatureEntity(digest, xRequestId);

        return qsealcSigner.getSignatureBase64(signatureEntity.toString().getBytes());
    }

    private RequestBuilder createRequest(final URL url, final String digest) {
        final String requestId = Psd2Headers.getRequestId();

        try {
            return client.request(url)
                    .type(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HeaderKeys.TENANT, HeaderValues.TENANT)
                    .header(HeaderKeys.X_REQUEST_ID, requestId)
                    .header(HeaderKeys.DIGEST, digest)
                    .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                    .header(
                            Psd2Headers.Keys.TPP_SIGNATURE_CERTIFICATE,
                            CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(qSealc))
                    .header(HeaderKeys.SIGNATURE, getAuthorization(digest, requestId));
        } catch (CertificateException e) {
            throw new IllegalStateException("Invalid qsealc detected", e);
        }
    }

    private RequestBuilder createRequestInSession(final URL url, final String digest) {

        RequestBuilder requestBuilder =
                createRequest(url, digest)
                        .header(HeaderKeys.CONSENT_ID, getConsentId())
                        .addBearerToken(
                                getTokenFromSession(BerlinGroupConstants.StorageKeys.OAUTH_TOKEN));

        if (isUserPresent) {
            requestBuilder.header(HeaderKeys.PSU_IP_ADDRESS, TriodosConstants.PSU_IPADDRESS);
        }
        return requestBuilder;
    }

    private X509Certificate getX509Certificate() {
        try {
            return CertificateUtils.getX509CertificatesFromBase64EncodedCert(getQSealc()).stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No certificate was found"));
        } catch (CertificateException ce) {
            throw new SecurityException("Certificate error", ce);
        }
    }
}
