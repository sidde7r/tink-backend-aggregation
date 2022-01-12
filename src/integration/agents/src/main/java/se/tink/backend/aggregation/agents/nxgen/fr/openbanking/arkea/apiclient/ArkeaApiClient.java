package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.apiclient;

import static javax.ws.rs.core.MediaType.*;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.*;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.HeaderKeys.*;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.HeaderValues.APPLICATION_HAL_JSON_CHARSET_VERSION;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.Urls.API_PSD_BASE_PATH;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.authenticator.ArkeaGetTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.authenticator.ArkeaRefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.authenticator.ArkeaTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.dto.ArkeaAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.dto.ArkeaBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.dto.ArkeaEndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.dto.ArkeaTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.dto.ArkeaTrustedBeneficiariesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrAispApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDtoBase;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class ArkeaApiClient implements FrAispApiClient {
    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final ArkeaSignatureHeaderCreator signatureHeaderCreator;

    public ArkeaTokenResponse exchangeAuthorizationCode(ArkeaGetTokenRequest tokenRequest) {
        return client.request(Urls.GET_AND_REFRESH_TOKEN_PATH)
                .body(tokenRequest)
                .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED)
                .accept(APPLICATION_JSON)
                .post(ArkeaTokenResponse.class);
    }

    public ArkeaTokenResponse refreshAccessToken(ArkeaRefreshTokenRequest tokenRequest) {
        return client.request(Urls.GET_AND_REFRESH_TOKEN_PATH)
                .body(tokenRequest)
                .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED)
                .accept(APPLICATION_JSON)
                .post(ArkeaTokenResponse.class);
    }

    public ArkeaAccountResponse getAccounts() {
        return createRequest(HttpMethod.GET, Urls.ACCOUNTS_PATH, ArkeaAccountResponse.class);
    }

    public ArkeaBalanceResponse getBalances(String accountResourceId) {
        return createRequest(
                HttpMethod.GET,
                String.format(
                        "%s/%s/%s", Urls.ACCOUNTS_PATH, accountResourceId, Urls.BALANCES_PATH),
                ArkeaBalanceResponse.class);
    }

    public ArkeaTransactionResponse getTransactions(String accountResourceId, String nextPagePath) {
        return nextPagePath == null
                ? createRequest(
                        HttpMethod.GET,
                        String.format(
                                "%s/%s/%s",
                                Urls.ACCOUNTS_PATH, accountResourceId, Urls.TRANSACTIONS_PATH),
                        ArkeaTransactionResponse.class)
                : createRequest(
                        HttpMethod.GET,
                        String.format("%s/%s", API_PSD_BASE_PATH, nextPagePath),
                        ArkeaTransactionResponse.class);
    }

    public ArkeaEndUserIdentityResponse getUserIdentity() {
        return createRequest(
                HttpMethod.GET, Urls.END_USER_IDENTITY_PATH, ArkeaEndUserIdentityResponse.class);
    }

    @Override
    public Optional<? extends TrustedBeneficiariesResponseDtoBase> getTrustedBeneficiaries() {
        return Optional.of(
                createRequest(
                        HttpMethod.GET,
                        Urls.TRUSTED_BENEFICIARIES_PATH,
                        ArkeaTrustedBeneficiariesResponse.class));
    }

    @Override
    public Optional<? extends TrustedBeneficiariesResponseDtoBase> getTrustedBeneficiaries(
            String path) {
        return Optional.of(
                createRequest(
                        HttpMethod.GET,
                        API_PSD_BASE_PATH + path,
                        ArkeaTrustedBeneficiariesResponse.class));
    }

    private <T> T createRequest(HttpMethod httpMethod, String path, Class<T> clazz) {
        final String requestId = UUID.randomUUID().toString();
        return client.request(path)
                .accept(APPLICATION_HAL_JSON_CHARSET_VERSION)
                .header(AUTHORIZATION, getTokenFromStorage().toAuthorizeHeader())
                .header(X_REQUEST_ID, requestId)
                .header(SIGNATURE, buildSignatureHeaderValue(httpMethod, path, requestId))
                .method(httpMethod, clazz);
    }

    private String buildSignatureHeaderValue(HttpMethod httpMethod, String path, String requestId) {
        return signatureHeaderCreator.createSignatureHeaderValue(httpMethod, path, requestId);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }
}
