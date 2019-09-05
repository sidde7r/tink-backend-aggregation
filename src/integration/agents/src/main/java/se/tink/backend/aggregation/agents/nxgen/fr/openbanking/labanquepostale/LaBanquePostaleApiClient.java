package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.Payload;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.configuration.LaBanquePostaleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountEntityBaseEntityWithHref;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class LaBanquePostaleApiClient
        extends BerlinGroupApiClient<LaBanquePostaleConfiguration> {

    public LaBanquePostaleApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public AccountResponse fetchAccounts() {
        AccountResponse accountResponse =
                buildRequestWithSignature(getConfiguration().getBaseUrl() + Urls.FETCH_ACCOUNTS, "")
                        .get(AccountResponse.class);

        accountResponse.getAccounts().forEach(this::populateBalanceForAccount);
        return accountResponse;
    }

    public TransactionResponse fetchTransactionsLaBanquePostal(String url) {
        if (url.startsWith("v1/")) {
            url =
                    url.substring(
                            3); // they return the url with prefix v1/ but api is without that url,
            // so if the prefix exists it is removed in this line
        }

        return buildRequestWithSignature(getConfiguration().getBaseUrl() + "/" + url, "")
                .get(TransactionResponse.class);
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(String url) {
        return null;
    }

    private RequestBuilder buildRequestWithSignature(final String url, final String payload) {
        final String reqId = BerlinGroupUtils.getRequestId();
        final String digest = generateDigest(payload);
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        final OAuth2Token token = getTokenFromSession(StorageKey.OAUTH_TOKEN);

        return client.request(url)
                .header(HeaderKeys.SIGNATURE, getAuthorization(digest, reqId))
                .addBearerToken(token)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.CLIENT_SECRET, clientSecret)
                .header(BerlinGroupConstants.HeaderKeys.X_REQUEST_ID, getX_Request_Id());
    }

    private String generateDigest(final String data) {
        return Signature.DIGEST_PREFIX + BerlinGroupUtils.calculateDigest(data);
    }

    private String getSignature(final String digest, String requestId) {
        final String clientSigningKeyPath = getConfiguration().getClientSigningKeyPath();

        final SignatureEntity signatureEntity = new SignatureEntity(digest, requestId);

        return BerlinGroupUtils.generateSignature(signatureEntity.toString(), clientSigningKeyPath);
    }

    private String getAuthorization(final String digest, String requestId) {
        final String clientId = getConfiguration().getClientId();

        return new AuthorizationEntity(clientId, getSignature(digest, requestId)).toString();
    }

    private String getX_Request_Id() {
        return UUID.randomUUID().toString();
    }

    @Override
    public OAuth2Token getToken(String code) {
        final String redirectUri = getConfiguration().getRedirectUrl();
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();
        TokenRequest tokenRequest =
                new TokenRequest(
                        LaBanquePostaleConstants.QueryValues.SCORE,
                        code,
                        QueryValues.GRANT_TYPE,
                        redirectUri);

        return client.request(getConfiguration().getOauthBaseUrl() + Urls.GET_TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .addBasicAuth(clientId, clientSecret)
                .post(TokenBaseResponse.class, tokenRequest.toData())
                .toTinkToken();
    }

    @Override
    public String getConsentId() {
        return null;
    }

    @Override
    public OAuth2Token refreshToken(String token) {
        return null;
    }

    @Override
    public URL getAuthorizeUrl(String state) {
        final String clientId = getConfiguration().getClientId();
        final String redirectUrl = getConfiguration().getRedirectUrl();

        return client.request(getConfiguration().getOauthBaseUrl() + Urls.OAUTH)
                .queryParam(BerlinGroupConstants.QueryKeys.CLIENT_ID, clientId)
                .queryParam(BerlinGroupConstants.QueryKeys.REDIRECT_URI, redirectUrl)
                .queryParam(
                        BerlinGroupConstants.QueryKeys.SCOPE,
                        LaBanquePostaleConstants.QueryValues.SCORE)
                .queryParam(BerlinGroupConstants.QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(BerlinGroupConstants.QueryKeys.STATE, state)
                .getUrl();
    }

    private AccountEntityBaseEntityWithHref populateBalanceForAccount(
            AccountEntity accountBaseEntityWithHref) {
        BalanceResponse balanceResponse =
                buildRequestWithSignature(
                                String.format(
                                        getConfiguration().getBaseUrl() + Urls.FETCH_BALANCES,
                                        accountBaseEntityWithHref.getResourceId()),
                                Payload.EMPTY)
                        .get(BalanceResponse.class);
        accountBaseEntityWithHref.setBalances(balanceResponse.getBalances());
        return accountBaseEntityWithHref;
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {
        return buildRequestWithSignature(
                        getConfiguration().getBaseUrl() + Urls.PAYMENT_INITIATION, Payload.EMPTY)
                .header(HeaderKeys.CONTENT_TYPE, HeaderValues.CONTENT_TYPE)
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public GetPaymentResponse getPayment(int paymentId) {
        return buildRequestWithSignature(
                        String.format(
                                getConfiguration().getBaseUrl() + Urls.GET_PAYMENT, paymentId),
                        Payload.EMPTY)
                .get(GetPaymentResponse.class);
    }

    public GetPaymentResponse confirmPayment(String paymentId) {
        return buildRequestWithSignature(
                        String.format(
                                getConfiguration().getBaseUrl() + Urls.CONFIRM_PAYMENT, paymentId),
                        Payload.EMPTY)
                .post(GetPaymentResponse.class);
    }
}
