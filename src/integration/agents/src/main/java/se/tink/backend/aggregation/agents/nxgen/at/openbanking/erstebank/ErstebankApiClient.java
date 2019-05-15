package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc.ConsentSignResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.configuration.ErstebankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenRequestGet;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class ErstebankApiClient extends BerlinGroupApiClient<ErstebankConfiguration> {
    public ErstebankApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public URL getAuthorizeUrl(String state) {
        final String authUrl = getConfiguration().getBaseUrl() + Urls.AUTH;
        return getAuthorizeUrl(
                        authUrl,
                        state,
                        getConfiguration().getClientId(),
                        getConfiguration().getRedirectUrl())
                .getUrl();
    }

    @Override
    public OAuth2Token getToken(String code) {
        final TokenRequestGet request =
                new TokenRequestGet(
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        getConfiguration().getRedirectUrl(),
                        code,
                        QueryValues.GRANT_TYPE);

        return client.request(
                        new URL(getConfiguration().getBaseUrl() + ErstebankConstants.Urls.TOKEN))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParams(request.toData())
                .get(TokenBaseResponse.class)
                .toTinkToken();
    }

    @Override
    public String getConsentId() {
        return getConsent(Collections.EMPTY_LIST).getConsentId();
    }

    public OAuth2Token refreshToken(String refreshToken) {
        final RefreshTokenRequest request =
                new RefreshTokenRequest(
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        QueryValues.GRANT_TYPE_REFRESH,
                        refreshToken);

        return client.request(
                        new URL(getConfiguration().getBaseUrl() + ErstebankConstants.Urls.TOKEN))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParams(request.toData())
                .get(TokenBaseResponse.class)
                .toTinkToken();
    }

    @Override
    public AccountsBaseResponse fetchAccounts() {
        return getAccountsRequestBuilder(getConfiguration().getBaseUrl() + Urls.ACCOUNTS)
                .header(HeaderKeys.WEB_API_KEY, getConfiguration().getApiKey())
                .get(AccountsBaseResponse.class);
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(String url) {
        return getTransactionsRequestBuilder(getConfiguration().getBaseUrl() + url)
                .header(HeaderKeys.WEB_API_KEY, getConfiguration().getApiKey())
                .get(TransactionsKeyPaginatorBaseResponse.class);
    }

    public ConsentBaseResponse getConsent(List<String> ibans) {
        return new ConsentBaseResponse("received", "1234-wertiq-983");

        //         TODO: Wait for erste's api to be fixed
        //
        //                return buildRequestForIbans(Urls.CONSENT, ibans)
        //                        .addBearerToken(getTokenFromSession(StorageKeys.OAUTH_TOKEN))
        //                        .header(HeaderKeys.WEB_API_KEY,
        // "a5ace35b-46aa-4d9f-ad9b-5b158a35a559")
        //                        .type(MediaType.APPLICATION_JSON)
        //                        .post(ConsentBaseResponse.class);
    }

    public ConsentSignResponse signConsent(String consentId) {
        return buildRequestWithSignature(
                        String.format(Urls.SIGN_CONSENT, consentId), FormValues.EMPTY)
                .header(HeaderKeys.WEB_API_KEY, getConfiguration().getApiKey())
                .type(MediaType.APPLICATION_JSON)
                .post(ConsentSignResponse.class);
    }

    private String getSignature(final String digest, String requestId) {
        final String clientSigningKeyPath = getConfiguration().getClientSigningKeyPath();

        final SignatureEntity signatureEntity = new SignatureEntity(digest, requestId);

        return BerlinGroupUtils.generateSignature(
                signatureEntity.toString(), clientSigningKeyPath, Signature.SIGNING_ALGORITHM);
    }

    private String getAuthorization(final String digest, String requestId) {
        final String clientId = getConfiguration().getClientId();

        return new AuthorizationEntity(clientId, getSignature(digest, requestId)).toString();
    }

    private String generateDigest(final String data) {
        return Signature.DIGEST_PREFIX + BerlinGroupUtils.calculateDigest(data);
    }

    private String getFormattedDate() {
        return BerlinGroupUtils.getFormattedCurrentDate(Signature.DATE_FORMAT, Signature.TIMEZONE);
    }

    private RequestBuilder buildRequest(
            final String date, final String digest, final String reqPath) {
        final String baseUrl = getConfiguration().getBaseUrl();

        return client.request(baseUrl + reqPath)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.DATE, date);
    }

    // Can't test it, since erste's api is not checking for signature
    private RequestBuilder buildRequestWithSignature(final String reqPath, final String payload) {
        final String reqId = BerlinGroupUtils.getRequestId();
        final String date = getFormattedDate();
        final String digest = generateDigest(payload);

        return buildRequest(date, digest, reqPath)
                .header(HeaderKeys.SIGNATURE, getAuthorization(digest, reqId))
                .header(HeaderKeys.CONSENT_ID, getConsentId())
                .addBearerToken(
                        sessionStorage
                                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        ErrorMessages.MISSING_TOKEN)));
    }

    private RequestBuilder buildRequestForIbans(final String reqPath, List<String> ibans) {
        final AccessEntity access = new AccessEntity();
        access.addIbans(ibans);
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);
        final String date = BerlinGroupUtils.formatDate(calendar.getTime(), "yyyy-MM-dd", null);

        final ConsentRequest consentRequest = new ConsentRequest();
        consentRequest.getAccess().addIbans(ibans);

        final String payload = consentRequest.toData();
        final String digest = generateDigest(payload);
        return buildRequest(date, digest, reqPath);
    }
}
