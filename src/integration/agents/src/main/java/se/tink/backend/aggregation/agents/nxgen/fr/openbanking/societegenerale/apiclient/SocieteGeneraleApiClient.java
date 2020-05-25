package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.Urls.BASE_URL;

import java.util.Base64;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transfer.rpc.TrustedBeneficiariesResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class SocieteGeneraleApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final SocieteGeneraleConfiguration configuration;
    private final SignatureHeaderProvider signatureHeaderProvider;

    public TokenResponse exchangeAuthorizationCodeOrRefreshToken(TokenRequest request) {
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

    public TrustedBeneficiariesResponse getTrustedBeneficiaries() {
        return getTrustedBeneficiaries(Urls.TRUSTED_BENEFICIARIES_PATH);
    }

    public TrustedBeneficiariesResponse getTrustedBeneficiaries(String path) {
        return getTrustedBeneficiaries(new URL(BASE_URL + path));
    }

    private TrustedBeneficiariesResponse getTrustedBeneficiaries(URL url) {
        return createRequest(url).get(TrustedBeneficiariesResponse.class);
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
