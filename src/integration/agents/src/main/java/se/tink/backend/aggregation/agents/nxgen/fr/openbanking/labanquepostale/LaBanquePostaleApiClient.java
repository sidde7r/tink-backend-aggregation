package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.Payload;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.configuration.LaBanquePostaleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.identity.dto.EndUserIdentityResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrAispApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class LaBanquePostaleApiClient extends BerlinGroupApiClient<LaBanquePostaleConfiguration>
        implements FrAispApiClient {

    private final QsealcSigner qsealcSigner;

    public LaBanquePostaleApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            QsealcSigner qsealcSigner,
            LaBanquePostaleConfiguration configuration,
            CredentialsRequest request,
            String redirectUrl,
            String qSealc) {
        super(client, persistentStorage, configuration, request, redirectUrl, qSealc);

        this.qsealcSigner = qsealcSigner;
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
        final Path transactionsPath = preparePathForFetchTransactions(url);

        return buildRequestWithSignature(
                        getConfiguration().getBaseUrl() + "/" + transactionsPath.toString(), "")
                .get(TransactionResponse.class);
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(String url) {
        return null;
    }

    public EndUserIdentityResponseDto getEndUserIdentity() {
        return buildRequestWithSignature(
                        getConfiguration().getBaseUrl() + Urls.FETCH_IDENTITY_DATA, "")
                .get(EndUserIdentityResponseDto.class);
    }

    private RequestBuilder buildRequestWithSignature(final String url, final String payload) {
        final String digest = generateDigest(payload);
        final String requestId = UUID.randomUUID().toString();

        final OAuth2Token token = getTokenFromSession(StorageKeys.OAUTH_TOKEN);

        return client.request(url)
                .header(HeaderKeys.PSU_DATE, LocalDate.now().toString())
                .header(HeaderKeys.SIGNATURE, getAuthorization(digest, requestId))
                .addBearerToken(token)
                .header(BerlinGroupConstants.HeaderKeys.X_REQUEST_ID, requestId);
    }

    private String generateDigest(final String data) {
        return Signature.DIGEST_PREFIX + Psd2Headers.calculateDigest(data);
    }

    private String getSignature(final String digest, String requestId) {
        final SignatureEntity signatureEntity = new SignatureEntity(digest, requestId);

        return qsealcSigner.getSignatureBase64(signatureEntity.toString().getBytes());
    }

    private String getAuthorization(final String digest, String requestId) {
        final String clientId = getConfiguration().getClientId();

        return new AuthorizationEntity(clientId, getSignature(digest, requestId)).toString();
    }

    @Override
    public OAuth2Token getToken(String code) {
        final String redirectUri = getRedirectUrl();
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();
        TokenRequest tokenRequest =
                new TokenRequest(
                        LaBanquePostaleConstants.QueryValues.SCORE,
                        code,
                        QueryValues.AUTHORIZATION_CODE,
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
        final String redirectUrl = getRedirectUrl();

        return client.request(getConfiguration().getOauthBaseUrl() + Urls.OAUTH)
                .queryParam(BerlinGroupConstants.QueryKeys.CLIENT_ID, clientId)
                .queryParam(BerlinGroupConstants.QueryKeys.REDIRECT_URI, redirectUrl)
                .queryParam(
                        BerlinGroupConstants.QueryKeys.SCOPE,
                        LaBanquePostaleConstants.QueryValues.SCORE)
                .queryParam(BerlinGroupConstants.QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(BerlinGroupConstants.QueryKeys.STATE, state)
                .getUrl();
    }

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries() {
        return getTrustedBeneficiaries(Urls.FETCH_TRUSTED_BENEFICIARIES);
    }

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries(String path) {
        final HttpResponse response =
                buildRequestWithSignature(getConfiguration().getBaseUrl() + path, "")
                        .get(HttpResponse.class);

        if (HttpStatus.SC_NO_CONTENT == response.getStatus()) {
            return Optional.empty();
        }

        return Optional.of(response.getBody(TrustedBeneficiariesResponseDto.class));
    }

    private void populateBalanceForAccount(AccountEntity accountBaseEntityWithHref) {
        BalanceResponse balanceResponse =
                buildRequestWithSignature(
                                String.format(
                                        getConfiguration().getBaseUrl() + Urls.FETCH_BALANCES,
                                        accountBaseEntityWithHref.getResourceId()),
                                Payload.EMPTY)
                        .get(BalanceResponse.class);
        accountBaseEntityWithHref.setBalances(balanceResponse.getBalances());
    }

    private Path preparePathForFetchTransactions(String url) {
        final Path startPath = Paths.get(url);
        final Path relativePath =
                startPath.isAbsolute() ? Paths.get("/").relativize(startPath) : startPath;

        return relativePath.startsWith("v1")
                ? relativePath.subpath(1, relativePath.getNameCount())
                : relativePath;
    }
}
