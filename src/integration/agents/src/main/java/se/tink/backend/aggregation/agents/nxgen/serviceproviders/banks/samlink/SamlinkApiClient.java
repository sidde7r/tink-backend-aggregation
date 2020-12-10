package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.httpclient.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConstants.Header;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConstants.LinkRel;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConstants.ServerError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.rpc.RegisterDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.Links;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard.entities.CreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard.rpc.CardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.loan.rpc.LoansResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.rpc.TransactionDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.LinksResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SamlinkApiClient {
    private final TinkHttpClient httpClient;
    private final SamlinkSessionStorage sessionStorage;
    private final SamlinkConfiguration agentConfiguration;

    private Links cachedApiEndpoints;

    public SamlinkApiClient(
            TinkHttpClient httpClient,
            SamlinkSessionStorage sessionStorage,
            SamlinkConfiguration agentConfiguration) {
        this.httpClient = httpClient;
        this.sessionStorage = sessionStorage;
        this.agentConfiguration = agentConfiguration;
    }

    public LoginResponse login(
            String username, String password, String deviceId, String deviceToken) {
        Links links = getApiEndpoints();

        LoginRequest loginRequest = new LoginRequest().setUsername(username).setPassword(password);

        if (!Strings.isNullOrEmpty(deviceId) && !Strings.isNullOrEmpty(deviceToken)) {
            loginRequest.setDeviceId(deviceId).setDeviceToken(deviceToken);
        }

        LoginResponse loginResponse;
        try {
            loginResponse =
                    buildRequest(links.getLinkPath(SamlinkConstants.LinkRel.IDENTIFICATION))
                            .post(LoginResponse.class, loginRequest);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                Optional<ServerError> errorResponse =
                        ErrorResponse.fromHttpResponseException(e).toUserError();
                ServerError serverError = errorResponse.orElseThrow(() -> e);
                throw serverError.exception();
            }
            throw e;
        }

        sessionStorage.storeAccessToken(
                loginResponse.getTokenType(), loginResponse.getAccessToken());

        // get the services endpoints and store it in sessionStorage
        Links serviceLinks = fetchServicesEndpoints(loginResponse.getLinks());
        sessionStorage.storeServicesEndpoints(serviceLinks);

        final String loginName = loginResponse.getName();
        if (!Strings.isNullOrEmpty(loginName)) {
            sessionStorage.storeLoginName(loginName);
        }

        return loginResponse;
    }

    public LoginResponse login(String username, String password) {
        // A new device/user will not have a deviceId or deviceToken yet.
        return login(username, password, null, null);
    }

    public RegisterDeviceResponse registerDevice(
            Links loginLinks, String codeCardValue, String deviceId) {
        URL authenticationUrl =
                agentConfiguration.build(
                        loginLinks.getLinkPath(SamlinkConstants.LinkRel.AUTHENTICATION));

        RegisterDeviceRequest registerDeviceRequest = new RegisterDeviceRequest();
        registerDeviceRequest.setCodeCardValue(codeCardValue).setDeviceId(deviceId);

        RegisterDeviceResponse registerDeviceResponse =
                buildRequest(authenticationUrl)
                        .post(RegisterDeviceResponse.class, registerDeviceRequest);

        // update session token
        sessionStorage.storeAccessToken(
                registerDeviceResponse.getTokenType(), registerDeviceResponse.getAccessToken());

        // get the services endpoints and store it in sessionStorage
        Links serviceLinks = fetchServicesEndpoints(registerDeviceResponse.getLinks());
        sessionStorage.storeServicesEndpoints(serviceLinks);

        return registerDeviceResponse;
    }

    public List<AccountEntity> getAccounts() {
        String servicesEndpoint = sessionStorage.getServicesEndpoint(LinkRel.ACCOUNTS);
        if (servicesEndpoint == null) {
            return new ArrayList<>();
        }
        return buildAccountRequest(servicesEndpoint).get(AccountsResponse.class).getAccounts();
    }

    public Optional<TransactionsResponse> getTransactions(TransactionalAccount account) {
        return Optional.ofNullable(account)
                .map(TransactionalAccount::getApiIdentifier)
                .map(link -> fetchTransactions(link, 0));
    }

    public Optional<TransactionsResponse> getTransactions(
            TransactionsResponse transactions, int offset) {
        return transactions.getNext().map(link -> fetchTransactions(link.getHref(), offset));
    }

    private TransactionsResponse fetchTransactions(String path, int offset) {
        return buildRequestWithLimitAndOffset(path, offset).get(TransactionsResponse.class);
    }

    private RequestBuilder buildRequestWithLimitAndOffset(String path, int offset) {
        return buildRequest(
                agentConfiguration
                        .build(path)
                        .queryParam(
                                SamlinkConstants.QueryParams.QUERY_PARAM_LIMIT,
                                SamlinkConstants.QueryParams.QUERY_PARAM_LIMIT_TX_DEFAULT)
                        .queryParam(
                                SamlinkConstants.QueryParams.QUERY_PARAM_OFFSET,
                                String.valueOf(offset)));
    }

    public TransactionDetailsResponse getTransactionDetails(Links transactionLinks) {
        return buildRequest(transactionLinks.getLinkPath(SamlinkConstants.LinkRel.DETAILS))
                .get(TransactionDetailsResponse.class);
    }

    public CreditCardsResponse getCreditCards() {
        String servicesEndpoint =
                sessionStorage.getServicesEndpoint(SamlinkConstants.LinkRel.CARDS);
        if (servicesEndpoint == null) {
            return null;
        }
        return buildAccountRequest(servicesEndpoint).get(CreditCardsResponse.class);
    }

    // The app requests 999 accounts, I don't see a need to page these
    private RequestBuilder buildAccountRequest(String servicesEndpoint) {
        URL creditCardsUrl =
                agentConfiguration
                        .build(servicesEndpoint)
                        .queryParam(
                                SamlinkConstants.QueryParams.QUERY_PARAM_LIMIT,
                                SamlinkConstants.QueryParams.QUERY_PARAM_LIMIT_ACCOUNT_DEFAULT)
                        .queryParam(
                                SamlinkConstants.QueryParams.QUERY_PARAM_OFFSET,
                                SamlinkConstants.QueryParams.QUERY_PARAM_OFFSET_DEFAULT);

        return buildRequest(creditCardsUrl);
    }

    public LoansResponse getLoans() {
        return buildRequest(sessionStorage.getServicesEndpoint(SamlinkConstants.LinkRel.LOANS))
                .get(LoansResponse.class);
    }

    public Optional<CardDetailsResponse> getCardDetails(CreditCard creditCard) {
        return creditCard
                .getDetailsLink()
                .map(
                        link ->
                                buildRequest(agentConfiguration.build(link.getHref()))
                                        .get(CardDetailsResponse.class));
    }

    public LoanDetailsEntity getLoanDetails(String detailsLink) {
        return buildRequest(agentConfiguration.build(detailsLink)).get(LoanDetailsEntity.class);
    }

    private RequestBuilder buildRequest(String path) {
        return buildRequest(agentConfiguration.build(path));
    }

    private RequestBuilder buildRequest(URL url) {
        final RequestBuilder requestBuilder = buildRequestHeaders(url, agentConfiguration.isV2());

        if (sessionStorage.hasAccessToken()) {
            return requestBuilder.header(
                    HttpHeaders.AUTHORIZATION, sessionStorage.getAccessToken());
        }
        return requestBuilder;
    }

    private RequestBuilder buildRequestHeaders(URL url, boolean isV2) {
        return isV2
                ? httpClient
                        .request(url)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(Header.VALUE_ACCEPT_V2)
                        .header(Header.CLIENT_VERSION, Header.VALUE_CLIENT_VERSION_V2)
                        .header(Header.CLIENT_APP, agentConfiguration.getClientApp())
                        .header(Header.API_KEY, Header.API_KEY_VALUE)
                : httpClient
                        .request(url)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(Header.VALUE_ACCEPT_V1)
                        .header(Header.CLIENT_VERSION, Header.VALUE_CLIENT_VERSION_V1);
    }

    private Links getApiEndpoints() {
        if (cachedApiEndpoints != null) {
            return cachedApiEndpoints;
        }

        LinksResponse linksResponse =
                buildRequest(SamlinkConstants.Url.BASE_PATH).get(LinksResponse.class);
        cachedApiEndpoints = linksResponse.getLinks();

        return cachedApiEndpoints;
    }

    private Links fetchServicesEndpoints(Links loginLinks) {
        return buildRequest(loginLinks.getLinkPath(SamlinkConstants.LinkRel.SERVICES))
                .get(LinksResponse.class)
                .getLinks();
    }

    public Optional<CreditCardTransactionsResponse> getTransactions(CreditCardAccount account) {
        return Optional.ofNullable(account)
                .map(CreditCardAccount::getApiIdentifier)
                .map(link -> fetchCreditCardTransactions(0, link));
    }

    public Optional<CreditCardTransactionsResponse> getTransactions(
            CreditCardTransactionsResponse creditCardTransactions, int offset) {
        return creditCardTransactions
                .getNext()
                .map(link -> fetchCreditCardTransactions(offset, link.getHref()));
    }

    private CreditCardTransactionsResponse fetchCreditCardTransactions(int offset, String link) {
        return buildRequestWithLimitAndOffset(link, offset)
                .get(CreditCardTransactionsResponse.class);
    }

    public void keepAlive() {
        buildRequest(sessionStorage.getServicesEndpoint(SamlinkConstants.LinkRel.COUNTS))
                .get(HttpResponse.class);
    }
}
