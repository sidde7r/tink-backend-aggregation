package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.Urls.AIS_BASE_URL;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.rpc.PisTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrAispApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class SocieteGeneraleApiClient implements FrAispApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final SocieteGeneraleConfiguration configuration;
    private final String redirectUrl;
    private final SignatureHeaderProvider signatureHeaderProvider;

    public TokenResponse exchangeAuthorizationCodeOrRefreshToken(AbstractForm request) {
        return client.request(new URL(SocieteGeneraleConstants.Urls.TOKEN_PATH))
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                        SocieteGeneraleConstants.HeaderKeys.AUTHORIZATION,
                        createAuthorizationBasicHeaderValue())
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        return createRequest(SocieteGeneraleConstants.Urls.ACCOUNTS_PATH)
                .get(AccountsResponse.class);
    }

    public TransactionsResponse getTransactions(String accountId, URL nextPageUrl) {
        RequestBuilder requestBuilder =
                nextPageUrl != null ? createRequest(nextPageUrl) : createFirstRequest(accountId);

        return requestBuilder.get(TransactionsResponse.class);
    }

    public EndUserIdentityResponse getEndUserIdentity() {
        return createRequest(Urls.END_USER_IDENTITY_PATH).get(EndUserIdentityResponse.class);
    }

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries() {
        return getTrustedBeneficiaries(Urls.TRUSTED_BENEFICIARIES_PATH);
    }

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries(String path) {
        return getTrustedBeneficiaries(new URL(AIS_BASE_URL + path));
    }

    private Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries(URL url) {
        return Optional.of(createRequest(url).get(TrustedBeneficiariesResponseDto.class));
    }

    private RequestBuilder createFirstRequest(String accountId) {
        return createRequest(
                SocieteGeneraleConstants.Urls.TRANSACTIONS_PATH.parameter(
                        SocieteGeneraleConstants.IdTags.ACCOUNT_RESOURCE_ID, accountId));
    }

    private RequestBuilder createRequest(URL url) {
        final String requestId = UUID.randomUUID().toString();
        final String signature = buildSignature(requestId);

        return client.request(url)
                .header(
                        SocieteGeneraleConstants.HeaderKeys.AUTHORIZATION,
                        createAuthorizationBearerHeaderValue())
                .header(SocieteGeneraleConstants.HeaderKeys.X_REQUEST_ID, requestId)
                .header(SocieteGeneraleConstants.HeaderKeys.SIGNATURE, signature)
                .header(SocieteGeneraleConstants.HeaderKeys.CLIENT_ID, configuration.getClientId())
                .accept(MediaType.APPLICATION_JSON);
    }

    private String createAuthorizationBasicHeaderValue() {
        return SocieteGeneraleConstants.HeaderValues.BASIC + " " + getEncodedAuthorizationString();
    }

    private String createAuthorizationBearerHeaderValue() {
        return SocieteGeneraleConstants.HeaderValues.BEARER + " " + getToken();
    }

    public GetPaymentResponse getPaymentStatus(String uniqueId) {
        return createRequestInSession(
                        Urls.FETCH_PAYMENT_STATUS.parameter(
                                SocieteGeneraleConstants.IdTags.PAYMENT_ID, uniqueId))
                .header(SocieteGeneraleConstants.HeaderKeys.PAYMENT_REQUEST_ID, uniqueId)
                .get(GetPaymentResponse.class);
    }

    public GetPaymentResponse confirmPayment(String uniqueId) {
        return createRequestInSession(
                        Urls.CONFIRM_PAYMENT.parameter(
                                SocieteGeneraleConstants.IdTags.PAYMENT_ID, uniqueId))
                .header(SocieteGeneraleConstants.HeaderKeys.PAYMENT_REQUEST_ID, uniqueId)
                .post(GetPaymentResponse.class);
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {

        return createRequestInSession(SocieteGeneraleConstants.Urls.PAYMENTS_PATH)
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    private RequestBuilder createRequestInSession(URL url) {
        String reqId = UUID.randomUUID().toString();
        String signature =
                signatureHeaderProvider.buildSignatureHeader(
                        persistentStorage.get(
                                SocieteGeneraleConstants.StorageKeys.PISP_OAUTH_TOKEN),
                        reqId);

        return client.request(url)
                .addBearerToken(
                        getPisOauthTokenFromStorage()
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        SocieteGeneraleConstants.ErrorMessages
                                                                .NO_ACCESS_TOKEN_IN_STORAGE)))
                .header(SocieteGeneraleConstants.HeaderKeys.X_REQUEST_ID, reqId)
                .header(SocieteGeneraleConstants.HeaderKeys.SIGNATURE, signature)
                .header(SocieteGeneraleConstants.HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(
                        SocieteGeneraleConstants.HeaderKeys.CONTENT_TYPE,
                        SocieteGeneraleConstants.HeaderValues.CONTENT_TYPE)
                .accept(MediaType.APPLICATION_JSON);
    }

    public void fetchPisAccessToken() {

        if (!isPisTokenValid()) {
            getAndSavePisToken();
        }
    }

    private void getAndSavePisToken() {
        PisTokenRequest request = new PisTokenRequest(redirectUrl);
        TokenResponse getTokenResponse = exchangeAuthorizationCodeOrRefreshToken(request);
        OAuth2Token token = getTokenResponse.toOauthToken();
        persistentStorage.put(SocieteGeneraleConstants.StorageKeys.PISP_OAUTH_TOKEN, token);
    }

    private boolean isPisTokenValid() {
        return getPisOauthTokenFromStorage().map(OAuth2TokenBase::isValid).orElse(false);
    }

    private Optional<OAuth2Token> getPisOauthTokenFromStorage() {
        return persistentStorage.get(
                SocieteGeneraleConstants.StorageKeys.PISP_OAUTH_TOKEN, OAuth2Token.class);
    }

    private String getAuthorizationString() {
        return configuration.getClientId() + ":" + configuration.getClientSecret();
    }

    private String getEncodedAuthorizationString() {
        return Base64.getEncoder().encodeToString(getAuthorizationString().getBytes());
    }

    private String buildSignature(String requestId) {
        return signatureHeaderProvider.buildSignatureHeader(getToken(), requestId);
    }

    private String getToken() {
        return persistentStorage.get(SocieteGeneraleConstants.StorageKeys.TOKEN);
    }
}
