package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.ApiServices.BASE_PATH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.ApiServices.BENEFICIARIES_PATH;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBranchConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.PutConsentsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrAispApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class CreditAgricoleBaseApiClient implements FrAispApiClient {

    private final TinkHttpClient client;
    private final CreditAgricoleStorage creditAgricoleStorage;
    private final AgentConfiguration<CreditAgricoleBaseConfiguration> agentConfiguration;
    private final CreditAgricoleBranchConfiguration branchConfiguration;

    public TokenResponse getToken(final String code) {
        final TokenRequest request = createAuthTokenRequest(code);
        final TokenResponse response = sendTokenRequest(request);

        creditAgricoleStorage.storeInitialFetchState(Boolean.TRUE);

        return response;
    }

    public OAuth2Token refreshToken(final String refreshToken) {
        final TokenRequest request = createRefreshTokenRequest(refreshToken);
        final TokenResponse response = sendTokenRequest(request);
        final OAuth2Token oAuth2Token = response.toTinkToken();

        creditAgricoleStorage.storeToken(oAuth2Token);
        creditAgricoleStorage.storeInitialFetchState(Boolean.FALSE);

        return oAuth2Token;
    }

    public GetAccountsResponse getAccounts() {
        return createGetRequest(CreditAgricoleBaseConstants.ApiServices.ACCOUNTS)
                .get(GetAccountsResponse.class);
    }

    public void putConsents(final List<AccountIdEntity> accountsToConsent) {
        createPutRequest().type(MediaType.APPLICATION_JSON).put(buildBody(accountsToConsent));
    }

    public GetTransactionsResponse getTransactions(
            final String id, final LocalDate dateFrom, final LocalDate dateTo) {
        final URL requestUrl = constructTransactionsURL(id, dateFrom, dateTo);
        final HttpResponse response = createGetRequest(requestUrl).get(HttpResponse.class);

        if (HttpStatus.SC_NO_CONTENT == response.getStatus()) {
            return new GetTransactionsResponse();
        }
        return response.getBody(GetTransactionsResponse.class);
    }

    public EndUserIdentityResponse getEndUserIdentity() {
        return createGetRequest(CreditAgricoleBaseConstants.ApiServices.FETCH_USER_IDENTITY_DATA)
                .get(EndUserIdentityResponse.class);
    }

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries() {
        return getTrustedBeneficiaries(BENEFICIARIES_PATH);
    }

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries(String path) {
        return getTrustedBeneficiaries(
                new URL(branchConfiguration.getBaseUrl() + BASE_PATH + path));
    }

    private Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries(URL url) {
        final HttpResponse response = createGetRequest(url).get(HttpResponse.class);

        if (HttpStatus.SC_NO_CONTENT == response.getStatus()) {
            return Optional.empty();
        }

        return Optional.of(response.getBody(TrustedBeneficiariesResponseDto.class));
    }

    private String getBearerAuthHeader() {
        return "Bearer " + creditAgricoleStorage.getTokenFromStorage();
    }

    private RequestBuilder createPutRequest() {
        return createRequest(
                new URL(
                        branchConfiguration.getBaseUrl()
                                + CreditAgricoleBaseConstants.ApiServices.CONSENTS));
    }

    private RequestBuilder createGetRequest(String path) {
        return createGetRequest(new URL(branchConfiguration.getBaseUrl() + path));
    }

    private RequestBuilder createGetRequest(URL url) {
        return createRequest(url).accept(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(CreditAgricoleBaseConstants.HeaderKeys.AUTHORIZATION, getBearerAuthHeader())
                .header(
                        CreditAgricoleBaseConstants.HeaderKeys.PSU_IP_ADDRESS,
                        CreditAgricoleBaseConstants.HeaderValues.PSU_IP_ADDRESS);
    }

    private TokenRequest createAuthTokenRequest(String authCode) {
        return new TokenRequest.TokenRequestBuilder()
                .scope(CreditAgricoleBaseConstants.QueryValues.SCOPE)
                .grantType(CreditAgricoleBaseConstants.QueryValues.GRANT_TYPE)
                .code(authCode)
                .redirectUri(agentConfiguration.getRedirectUrl())
                .clientId(agentConfiguration.getProviderSpecificConfiguration().getClientId())
                .build();
    }

    private TokenRequest createRefreshTokenRequest(String refreshToken) {
        return new TokenRequest.TokenRequestBuilder()
                .scope(CreditAgricoleBaseConstants.QueryValues.SCOPE)
                .grantType(CreditAgricoleBaseConstants.QueryValues.REFRESH_TOKEN)
                .refreshToken(refreshToken)
                .redirectUri(agentConfiguration.getRedirectUrl())
                .clientId(agentConfiguration.getProviderSpecificConfiguration().getClientId())
                .build();
    }

    private TokenResponse sendTokenRequest(TokenRequest tokenRequest) {
        try {
            return client.request(
                            branchConfiguration.getBaseUrl()
                                    + CreditAgricoleBaseConstants.ApiServices.TOKEN)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(
                            CreditAgricoleBaseConstants.HeaderKeys.CORRELATION_ID,
                            UUID.randomUUID().toString())
                    .header(
                            CreditAgricoleBaseConstants.HeaderKeys.CATS_CONSOMMATEUR,
                            CreditAgricoleBaseConstants.HeaderValues.CATS_CONSOMMATEUR)
                    .header(
                            CreditAgricoleBaseConstants.HeaderKeys.CATS_CONSOMMATEURORIGINE,
                            CreditAgricoleBaseConstants.HeaderValues.CATS_CONSOMMATEURORIGINE)
                    .header(
                            CreditAgricoleBaseConstants.HeaderKeys.CATS_CANAL,
                            CreditAgricoleBaseConstants.HeaderValues.CATS_CANAL)
                    .post(TokenResponse.class, tokenRequest.toData());
        } catch (HttpClientException ex) {
            if (ex.getMessage().contains("failed to respond")) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            throw ex;
        }
    }

    private URL constructTransactionsURL(String id, LocalDate dateFrom, LocalDate dateTo) {
        return new URL(
                        branchConfiguration.getBaseUrl()
                                + CreditAgricoleBaseConstants.ApiServices.TRANSACTIONS)
                .parameter(CreditAgricoleBaseConstants.IdTags.ACCOUNT_ID, id)
                .queryParam(CreditAgricoleBaseConstants.QueryKeys.DATE_FROM, dateFrom.toString())
                .queryParam(CreditAgricoleBaseConstants.QueryKeys.DATE_TO, dateTo.toString());
    }

    private static PutConsentsRequest buildBody(
            final List<AccountIdEntity> listOfNecessaryConsents) {
        return new PutConsentsRequest(listOfNecessaryConsents, listOfNecessaryConsents, true, true);
    }
}
