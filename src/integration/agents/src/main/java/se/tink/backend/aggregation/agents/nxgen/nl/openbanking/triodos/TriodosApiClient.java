package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.configuration.TriodosConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.Certificate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class TriodosApiClient extends BerlinGroupApiClient<TriodosConfiguration> {

    private Credentials credentials;

    public TriodosApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public AccountsBaseResponse fetchAccounts() {
        final String digest = BerlinGroupUtils.calculateDigest(FormValues.EMPTY);
        final URL accountsUrl = new URL(getConfiguration().getBaseUrl() + Urls.ACCOUNTS);
        final AccountsBaseResponse res =
                createRequestInSession(accountsUrl, digest)
                        .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                        .get(AccountsBaseResponse.class);

        final List<AccountBaseEntity> accountsWithBalances =
                res.getAccounts().stream().map(this::fetchBalances).collect(Collectors.toList());

        return new AccountsBaseResponse(accountsWithBalances);
    }

    public URL getAuthorizeUrl(String state) {
        final String consentId = getConsentId();
        final String codeVerifier = BerlinGroupUtils.generateCodeVerifier();
        sessionStorage.put(BerlinGroupConstants.StorageKeys.CODE_VERIFIER, codeVerifier);
        final String codeChallenge = BerlinGroupUtils.generateCodeChallenge(codeVerifier);
        sessionStorage.put(BerlinGroupConstants.StorageKeys.CONSENT_ID, consentId);
        final String authUrl = getConfiguration().getBaseUrl() + Urls.AUTH;

        return getAuthorizeUrl(
                        authUrl,
                        state,
                        getConfiguration().getClientId(),
                        getConfiguration().getRedirectUrl())
                .queryParam(QueryKeys.SCOPE, TriodosConstants.QueryValues.SCOPE + consentId)
                .queryParam(QueryKeys.CODE_CHALLENGE, codeChallenge)
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .getUrl();
    }

    private AccountBaseEntity fetchBalances(AccountBaseEntity accountBaseEntity) {
        final String digest = BerlinGroupUtils.calculateDigest(FormValues.EMPTY);
        final URL url =
                new URL(
                        getConfiguration().getBaseUrl()
                                + Urls.AIS_BASE
                                + accountBaseEntity.getBalancesLink());
        final List<BalanceBaseEntity> balances =
                createRequestInSession(url, digest).get(AccountBaseEntity.class).getBalances();
        accountBaseEntity.setBalances(balances);

        return accountBaseEntity;
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(String url) {
        final String digest = BerlinGroupUtils.calculateDigest(FormValues.EMPTY);
        final URL fullUrl = new URL(getConfiguration().getBaseUrl() + Urls.AIS_BASE + url);

        return createRequestInSession(fullUrl, digest)
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .queryParam(
                        TriodosConstants.QueryKeys.DATE_FROM,
                        TriodosConstants.QueryValues.DATE_FROM)
                .get(TransactionsKeyPaginatorBaseResponse.class);
    }

    @Override
    public OAuth2Token getToken(String code) {
        final String codeVerifier =
                sessionStorage.get(BerlinGroupConstants.StorageKeys.CODE_VERIFIER);
        final String body =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.CLIENT_CREDENTIALS)
                        .put(FormKeys.REDIRECT_URI, getConfiguration().getRedirectUrl())
                        .put(FormKeys.CODE_VERIFIER, codeVerifier)
                        .put(FormKeys.CODE, code)
                        .build()
                        .serialize();
        final TokenBaseResponse token =
                client.request(getConfiguration().getBaseUrl() + Urls.TOKEN)
                        .addBasicAuth(
                                getConfiguration().getClientId(),
                                getConfiguration().getClientSecret())
                        .header(HeaderKeys.SSL_CERTIFICATE, getCertificateEncoded())
                        .body(body)
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .post(TokenBaseResponse.class);
        setTokenToSession(token.toTinkToken(), BerlinGroupConstants.StorageKeys.OAUTH_TOKEN);
        authorizeConsent();

        return token.toTinkToken();
    }

    @Override
    public String getConsentId() {
        final ConsentRequest consentsRequest = new ConsentRequest();
        consentsRequest.getAccess().addIban(credentials.getField("IBAN"));
        final String digest = BerlinGroupUtils.calculateDigest(consentsRequest.toData());
        if (StringUtils.isNotEmpty(
                sessionStorage.get(BerlinGroupConstants.StorageKeys.CONSENT_ID))) {
            return sessionStorage.get(BerlinGroupConstants.StorageKeys.CONSENT_ID);
        }
        final URL url = new URL(getConfiguration().getBaseUrl() + Urls.CONSENT);
        final ConsentResponse consentResponse =
                createRequest(url, digest)
                        .body(consentsRequest.toData())
                        .header(HeaderKeys.PSU_IP_ADDRESS, getConfiguration().getPsuIpAddress())
                        .post(ConsentResponse.class);

        sessionStorage.put(
                BerlinGroupConstants.StorageKeys.CONSENT_ID, consentResponse.getConsentId());
        sessionStorage.put(
                TriodosConstants.HeaderKeys.AUTHORIZATION_ID, consentResponse.getAuthorisationId());

        return consentResponse.getConsentId();
    }

    private void authorizeConsent() {
        final URL url =
                new URL(
                        getConfiguration().getBaseUrl()
                                + String.format(
                                        Urls.AUTHORIZE_CONSENT,
                                        sessionStorage.get(
                                                BerlinGroupConstants.StorageKeys.CONSENT_ID),
                                        sessionStorage.get(StorageKeys.AUTHORIZATION_ID)));
        createRequestInSession(url, FormValues.EMPTY).put(String.class);
    }

    @Override
    public OAuth2Token refreshToken(String token) {
        return null;
    }

    private String getAuthorization(final String digest, final String xRequestId) {
        return new AuthorizationEntity(
                        getConfiguration().getApiKey(), getSignature(digest, xRequestId))
                .toString();
    }

    private String getSignature(final String digest, final String xRequestId) {
        final String clientSigningKeyPath = getConfiguration().getClientSigningKeyPath();
        final SignatureEntity signatureEntity = new SignatureEntity(digest, xRequestId);

        return BerlinGroupUtils.generateSignature(
                signatureEntity.toString(), clientSigningKeyPath, Signature.SIGNING_ALGORITHM);
    }

    private RequestBuilder createRequest(URL url, String digest) {
        final String requestId = UUID.randomUUID().toString();

        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.TPP_REDIRECT_URI, getConfiguration().getRedirectUrl())
                .header(HeaderKeys.SSL_CERTIFICATE, getCertificateEncoded())
                .header(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, getCertificateEncoded())
                .header(HeaderKeys.SIGNATURE, getAuthorization(digest, requestId));
    }

    private RequestBuilder createRequestInSession(URL url, String digest) {
        return createRequest(url, digest)
                .header(HeaderKeys.CONSENT_ID, getConsentId())
                .addBearerToken(getTokenFromSession(BerlinGroupConstants.StorageKeys.OAUTH_TOKEN));
    }

    private String getCertificateEncoded() {
        final String certificate =
                new String(
                        BerlinGroupUtils.readFile(
                                getConfiguration().getClientSigningCertificatePath()));
        try {

            return URLEncoder.encode(certificate, Certificate.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(ErrorMessages.ENCODE_CERTIFICATE_ERROR, e);
        }
    }
}
