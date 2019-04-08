package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import com.google.api.client.util.Strings;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.Base64;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.ExchangeAuthorizationCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.TokenResponse;
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

    RabobankApiClient(final TinkHttpClient client, final PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public TokenResponse exchangeAuthorizationCode(final ExchangeAuthorizationCodeRequest request) {
        return post(request);
    }

    public TokenResponse refreshAccessToken(final RefreshTokenRequest request) {
        return post(request);
    }

    private TokenResponse post(final AbstractForm request) {
        return client.request(RabobankConstants.URLs.OAUTH2_TOKEN_RABOBANK)
                .body(request, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addBasicAuth(getClientId(), getClientSecret())
                .post(TokenResponse.class);
    }

    private String getClientId() {
        final String clientId = persistentStorage.get(RabobankConstants.StorageKey.CLIENT_ID);
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalStateException("clientId is null or empty!");
        }

        return clientId;
    }

    private String getClientSecret() {
        final String clientId = persistentStorage.get(RabobankConstants.StorageKey.CLIENT_SECRET);
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalStateException("client secret is null or empty!");
        }

        return clientId;
    }

    private RequestBuilder buildRequest(
            final URL url,
            final String requestId,
            final String digest,
            final String signatureHeader,
            final String date) {
        final String digestHeader = RabobankConstants.Signature.SIGNING_STRING_SHA_512 + digest;
        return client.request(url)
                .addBearerToken(RabobankUtils.getOauthToken(persistentStorage))
                .header(
                        RabobankConstants.QueryParams.IBM_CLIENT_ID,
                        persistentStorage.get(RabobankConstants.StorageKey.CLIENT_ID))
                .header(
                        RabobankConstants.QueryParams.TPP_SIGNATURE_CERTIFICATE,
                        persistentStorage.get(RabobankConstants.StorageKey.CLIENT_CERT))
                .header(RabobankConstants.QueryParams.REQUEST_ID, requestId)
                .header(RabobankConstants.QueryParams.DIGEST, digestHeader)
                .header(RabobankConstants.QueryParams.SIGNATURE, signatureHeader)
                .header(RabobankConstants.QueryParams.DATE, date)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public TransactionalAccountsResponse fetchAccounts() {
        final String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        final String uuid = RabobankUtils.getRequestId();
        final String date = RabobankUtils.getDate();
        final String signatureHeader = buildSignatureHeader(digest, uuid, date);

        return buildRequest(
                        RabobankConstants.URLs.AIS_RABOBANK_ACCOUNTS,
                        uuid,
                        digest,
                        signatureHeader,
                        date)
                .get(TransactionalAccountsResponse.class);
    }

    public BalanceResponse getBalance(final String accountId) {
        final String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        final String uuid = RabobankUtils.getRequestId();
        final String date = RabobankUtils.getDate();
        final String signatureHeader = buildSignatureHeader(digest, uuid, date);

        return buildRequest(
                        RabobankConstants.URLs.buildBalanceUrl(accountId),
                        uuid,
                        digest,
                        signatureHeader,
                        date)
                .get(BalanceResponse.class);
    }

    private PrivateKey getPrivateKey() {
        final byte[] pkcs12 =
                Base64.getDecoder()
                        .decode(persistentStorage.get(RabobankConstants.StorageKey.CLIENT_SSL_P12));
        return RabobankUtils.getPrivateKey(
                pkcs12,
                persistentStorage.get(RabobankConstants.StorageKey.CLIENT_CERT_KEY_PASSWORD));
    }

    public TransactionalTransactionsResponse getTransactions(
            final TransactionalAccount account, final int page) {

        final String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        final String uuid = RabobankUtils.getRequestId();
        final String date = RabobankUtils.getDate();
        final String signatureHeader = buildSignatureHeader(digest, uuid, date);

        final String accountId =
                account.getFromTemporaryStorage(RabobankConstants.StorageKey.RESOURCE_ID);
        final SimpleDateFormat sdf =
                new SimpleDateFormat(RabobankConstants.TRANSACTION_DATE_FORMAT);

        return buildRequest(
                        RabobankConstants.URLs.buildTransactionsUrl(accountId),
                        uuid,
                        digest,
                        signatureHeader,
                        date)
                .queryParam(
                        RabobankConstants.QueryParams.BOOKING_STATUS,
                        RabobankConstants.QueryValues.BOTH)
                .queryParam(RabobankConstants.QueryParams.PAGE, Integer.toString(page))
                .queryParam(
                        RabobankConstants.QueryParams.SIZE,
                        RabobankConstants.QueryValues.TRANSACTIONS_SIZE)
                .get(TransactionalTransactionsResponse.class);
    }

    private String buildSignatureHeader(
            final String digest, final String requestId, final String date) {
        final String signingString = RabobankUtils.createSignatureString(date, digest, requestId);
        final byte[] signatureBytes = RSA.signSha512(getPrivateKey(), signingString.getBytes());
        final String b64Signature = Base64.getEncoder().encodeToString(signatureBytes);
        return RabobankUtils.createSignatureHeader(
                persistentStorage.get(RabobankConstants.StorageKey.CLIENT_CERT_SERIAL),
                RabobankConstants.Signature.RSA_SHA_512,
                b64Signature,
                RabobankConstants.Signature.HEADERS_VALUE);
    }
}
