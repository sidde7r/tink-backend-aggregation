package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.Base64;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.URLs;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.ExchangeAuthorizationCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.TransactionalAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.TransactionalTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.utils.RabobankUtils;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RabobankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private RabobankConfiguration configuration;

    RabobankApiClient(final TinkHttpClient client, final PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public RabobankConfiguration getConfiguration() {
        return new RabobankConfiguration();
    }

    public void setConfiguration(RabobankConfiguration configuration) {
        this.configuration = configuration;
    }

    public TokenResponse exchangeAuthorizationCode(final ExchangeAuthorizationCodeRequest request) {
        return post(request);
    }

    public TokenResponse refreshAccessToken(final RefreshTokenRequest request) {
        return post(request);
    }

    private TokenResponse post(final AbstractForm request) {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        return client.request(URLs.OAUTH2_TOKEN_RABOBANK)
                .body(request, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addBasicAuth(clientId, clientSecret)
                .post(TokenResponse.class);
    }

    private RequestBuilder buildRequest(
            final URL url,
            final String requestId,
            final String digest,
            final String signatureHeader,
            final String date) {
        final String clientId = getConfiguration().getClientId();
        final String clientCert = getConfiguration().getClientCert();
        final String digestHeader = Signature.SIGNING_STRING_SHA_512 + digest;

        return client.request(url)
                .addBearerToken(RabobankUtils.getOauthToken(persistentStorage))
                .header(QueryParams.IBM_CLIENT_ID, clientId)
                .header(QueryParams.TPP_SIGNATURE_CERTIFICATE, clientCert)
                .header(QueryParams.REQUEST_ID, requestId)
                .header(QueryParams.DIGEST, digestHeader)
                .header(QueryParams.SIGNATURE, signatureHeader)
                .header(QueryParams.DATE, date)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public TransactionalAccountsResponse fetchAccounts() {
        final String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        final String uuid = RabobankUtils.getRequestId();
        final String date = RabobankUtils.getDate();
        final String signatureHeader = buildSignatureHeader(digest, uuid, date);

        return buildRequest(URLs.AIS_RABOBANK_ACCOUNTS, uuid, digest, signatureHeader, date)
                .get(TransactionalAccountsResponse.class);
    }

    public BalanceResponse getBalance(final String accountId) {
        final String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        final String uuid = RabobankUtils.getRequestId();
        final String date = RabobankUtils.getDate();
        final String signatureHeader = buildSignatureHeader(digest, uuid, date);

        return buildRequest(URLs.buildBalanceUrl(accountId), uuid, digest, signatureHeader, date)
                .get(BalanceResponse.class);
    }

    private PrivateKey getPrivateKey() {
        final byte[] pkcs12 = getConfiguration().getClientSSLP12bytes();
        final String clientSSLKeyPassword = getConfiguration().getClientSSLKeyPassword();

        return RabobankUtils.getPrivateKey(pkcs12, clientSSLKeyPassword);
    }

    public TransactionalTransactionsResponse getTransactions(
            final TransactionalAccount account, final int page) {
        final String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        final String uuid = RabobankUtils.getRequestId();
        final String date = RabobankUtils.getDate();
        final String signatureHeader = buildSignatureHeader(digest, uuid, date);

        final String accountId = account.getFromTemporaryStorage(StorageKey.RESOURCE_ID);
        final SimpleDateFormat sdf =
                new SimpleDateFormat(RabobankConstants.TRANSACTION_DATE_FORMAT);

        return buildRequest(
                        URLs.buildTransactionsUrl(accountId), uuid, digest, signatureHeader, date)
                .queryParam(QueryParams.BOOKING_STATUS, QueryValues.BOTH)
                .queryParam(QueryParams.PAGE, Integer.toString(page))
                .queryParam(QueryParams.SIZE, QueryValues.TRANSACTIONS_SIZE)
                .get(TransactionalTransactionsResponse.class);
    }

    private String buildSignatureHeader(
            final String digest, final String requestId, final String date) {
        final String signingString = RabobankUtils.createSignatureString(date, digest, requestId);
        final byte[] signatureBytes = RSA.signSha512(getPrivateKey(), signingString.getBytes());
        final String b64Signature = Base64.getEncoder().encodeToString(signatureBytes);
        final String clientCertSerial = getConfiguration().getClientCertSerial();

        return RabobankUtils.createSignatureHeader(
                clientCertSerial, Signature.RSA_SHA_512, b64Signature, Signature.HEADERS_VALUE);
    }
}
