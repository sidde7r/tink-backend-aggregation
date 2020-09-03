package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.Range;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.RequestValues;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.entities.Characteristics;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.entities.ITEntityType;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.IdentificationRoutingRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.IdentificationRoutingResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.OAuth2TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.SsoBapiRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary.rpc.CaisseEpargneCreateBeneficiaryRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary.rpc.CaisseEpargneCreateBeneficiaryResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.AccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.AccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.utils.SoapHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.BpceConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceCookieParserHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceTokenExtractor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.entities.MembershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.configuration.BpceConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.storage.BpceStorage;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CaisseEpargneApiClient extends BpceApiClient {

    private static final String EXPECTED_REDIRECT_GOT = "Expected redirect, got: ";

    CaisseEpargneApiClient(
            TinkHttpClient httpClient,
            BpceConfiguration bpceConfiguration,
            RandomValueGenerator randomValueGenerator,
            BpceStorage bpceStorage,
            BpceTokenExtractor bpceTokenExtractor,
            BpceCookieParserHelper bpceCookieParserHelper) {
        super(
                httpClient,
                bpceConfiguration,
                randomValueGenerator,
                bpceStorage,
                bpceTokenExtractor,
                bpceCookieParserHelper);
    }

    public Optional<OAuth2Token> getOAuth2Token() {
        return Optional.ofNullable(
                httpClient
                        .request(Urls.OAUTH2_TOKEN)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .acceptLanguage(Locale.US)
                        .body(new TokenRequest())
                        .post(OAuth2TokenResponse.class)
                        .toTinkToken());
    }

    public IdentificationRoutingResponse identificationRouting(
            String userCode, OAuth2Token bearerToken) {
        IdentificationRoutingRequest request =
                IdentificationRoutingRequest.builder()
                        .characteristics(
                                Characteristics.builder()
                                        .bankId("")
                                        .subscribeTypeItems(Collections.emptyList())
                                        .iTEntityType(
                                                ITEntityType.builder()
                                                        .code(RequestValues.IT_ENTITY_02)
                                                        .build())
                                        .userCode(userCode)
                                        .build())
                        .build();

        return httpClient
                .request(Urls.IDENTIFICATION_ROUTING)
                .addBearerToken(bearerToken)
                .acceptLanguage(Locale.US)
                .accept(MediaType.WILDCARD)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(request)
                .post(IdentificationRoutingResponse.class);
    }

    public String oAuth2AuthorizeRedirect(
            String username, String bankId, MembershipType membershipType, String idTokenHint) {

        HttpResponse httpResponse =
                prepareAuthorizeRequest(bankId, username, membershipType, idTokenHint)
                        .get(HttpResponse.class);
        if (!Range.between(300, 399).contains(httpResponse.getStatus())) {
            throw new IllegalStateException(EXPECTED_REDIRECT_GOT + httpResponse.getStatus());
        }
        httpResponse =
                httpClient
                        .request(new URL(httpResponse.getLocation().toString()))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(HttpResponse.class);
        URI location = httpResponse.getLocation();
        String query = location.getQuery();
        return Arrays.stream(query.split("&"))
                .map(string -> string.split("="))
                .filter(element -> element.length == 2)
                .filter(element -> QueryKeys.TRANSACTION_ID.equalsIgnoreCase(element[0]))
                .map(element -> element[1])
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not parse transaction ID from location: "
                                                + location.toString()));
    }

    public String soapActionSsoBapi(String bankId) {
        OAuth2Token token = getTokenFromStorage();
        String termId = bpceStorage.getTermId();
        SsoBapiRequest ssoBapiRequest = new SsoBapiRequest(token.getAccessToken(), termId);
        HttpResponse response =
                httpClient
                        .request(Urls.WS_BAD)
                        .header(HeaderKeys.SOAP_ACTION, ssoBapiRequest.soapAction())
                        .header(HeaderKeys.VERSION_WS_BAD, HeaderValues.VERSION_WS_BAD_22)
                        .header(HeaderKeys.ESTABLISHMENT_ID, bankId)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.TEXT_XML)
                        .accept(MediaType.WILDCARD_TYPE)
                        .body(SoapHelper.formRequest(ssoBapiRequest))
                        .post(HttpResponse.class);
        return response.getBody(String.class);
    }

    public AccountsResponse getAccounts() {
        AccountsRequest accountsRequest = new AccountsRequest();
        String accountsResponse =
                httpClient
                        .request(Urls.WS_BAD)
                        .header(HeaderKeys.SOAP_ACTION, accountsRequest.soapAction())
                        .accept(MediaType.WILDCARD_TYPE)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.TEXT_XML)
                        .body(SoapHelper.formRequest(accountsRequest))
                        .post(String.class);
        return SoapHelper.getAccounts(accountsResponse);
    }

    public AccountDetailsResponse getAccountDetails(String fullAccountNumber) {
        AccountDetailsRequest accountDetailsRequest = new AccountDetailsRequest(fullAccountNumber);
        String accountDetailsResponse =
                httpClient
                        .request(Urls.WS_BAD)
                        .header(HeaderKeys.SOAP_ACTION, accountDetailsRequest.soapAction())
                        .accept(MediaType.WILDCARD_TYPE)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.TEXT_XML)
                        .body(SoapHelper.formRequest(accountDetailsRequest))
                        .post(String.class);
        return SoapHelper.getAccountDetails(accountDetailsResponse);
    }

    public TransactionsResponse getTransactionsForAccount(String fullAccountNumber, String key) {
        TransactionsRequest transactionsRequest = new TransactionsRequest();
        transactionsRequest.setAccount(fullAccountNumber);
        transactionsRequest.setPaginationKey(key);
        String request = SoapHelper.formRequest(transactionsRequest);
        String transactionsResponse =
                httpClient
                        .request(Urls.WS_BAD)
                        .accept(MediaType.WILDCARD_TYPE)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.TEXT_XML)
                        .header(HeaderKeys.SOAP_ACTION, transactionsRequest.soapAction())
                        .body(request)
                        .post(String.class);
        return SoapHelper.getTransactions(transactionsResponse);
    }

    public CaisseEpargneCreateBeneficiaryResponse createBeneficiary(
            CaisseEpargneCreateBeneficiaryRequest request) {

        final String url = bpceConfiguration.getRsExAthBaseUrl() + BpceConstants.BENEFICIARIES_PATH;

        HttpResponse response;
        try {
            response =
                    httpClient
                            .request(url)
                            .body(request)
                            .addBearerToken(getTokenFromStorage())
                            .accept(MediaType.WILDCARD_TYPE)
                            .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .header(HeaderKeys.X_SECURE_PASS_TYPE, "out-band")
                            .post(HttpResponse.class);
        } catch (HttpResponseException e) {
            // Forbidden means we have insufficient authentication, so we handle it in this method.
            if (e.getResponse().getStatus() != HttpStatus.SC_FORBIDDEN
                    && e.getResponse().getStatus() != HttpStatus.SC_BAD_REQUEST) {
                throw e;
            }
            response = e.getResponse();
        }
        CaisseEpargneCreateBeneficiaryResponse createBeneficiaryResponse =
                response.getBody(CaisseEpargneCreateBeneficiaryResponse.class);
        createBeneficiaryResponse.setIdTokenHint(
                response.getHeaders().getFirst(HeaderKeys.X_STEP_UP_TOKEN));
        return createBeneficiaryResponse;
    }
}
