package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian;

import com.google.common.collect.Lists;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianConstants.ElementAttributes;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianConstants.ElementNames;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.LoginParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.rpc.CollectBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.rpc.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.rpc.LoginStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.rpc.OrderBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.rpc.OrderBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.entity.Form;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.common.entity.AccountInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.common.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.creditcard.entity.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.creditcard.entity.CreditCardOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.creditcard.entity.CreditCardResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.savingsaccount.entity.SavingsAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.handler.NorwegianRedirectHandler;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.jsoup.ElementUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class NorwegianApiClient {

    private final TinkHttpClient client;

    public NorwegianApiClient(TinkHttpClient client) {
        this.client = client;
        client.addRedirectHandler(new NorwegianRedirectHandler(client));
        client.setCookieSpec(CookieSpecs.STANDARD);
    }

    private RequestBuilder createRequest(String url) {
        return createRequest(new URL(url));
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createScrapeRequest(String url) {
        return client.request(url).accept(MediaType.TEXT_HTML);
    }

    public String fetchLoginReturnUrl() {
        HttpResponse response =
                client.request(NorwegianConstants.Urls.INIT_URL).get(HttpResponse.class);

        return response.getRedirects().stream()
                .map(uri -> URLEncodedUtils.parse(uri, NorwegianConstants.URL_ENCODING))
                .flatMap(List::stream)
                .filter(pair -> QueryKeys.RETURN_URL.equals(pair.getName()))
                .findFirst()
                .map(NameValuePair::getValue)
                .map(EncodingUtils::encodeUrl)
                .orElseThrow(NoSuchElementException::new);
    }

    public String fetchBankIdInitPage(String returnUrl) {
        String targetUrl = Urls.LOGIN_URL.concat(returnUrl);
        HttpResponse httpResponse = client.request(targetUrl).get(HttpResponse.class);
        return httpResponse.getBody(String.class);
    }

    public OrderBankIdResponse orderBankId(String bankIdUrl, String ssn) {
        OrderBankIdRequest request = new OrderBankIdRequest(ssn);

        return createRequest(new URL(bankIdUrl + NorwegianConstants.Urls.ORDER))
                .post(OrderBankIdResponse.class, request);
    }

    public CollectBankIdResponse collectBankId(String collectUrl, CollectBankIdRequest request) {
        return createRequest(new URL(collectUrl)).post(CollectBankIdResponse.class, request);
    }

    public String completeBankId(String completeUrl) {
        // Initiate the SAML request.
        String completeBankIdResponse = createRequest(completeUrl).get(String.class);
        Document completeDocument = Jsoup.parse(completeBankIdResponse);
        Element formElement = completeDocument.getElementById(ElementNames.SAML_FORM);

        // Use the SAML created secret key to authenticate the user.
        return createFormRequest(
                        formElement.attr(ElementAttributes.ACTION),
                        ElementUtils.parseFormParameters(formElement))
                .post(String.class);
    }

    public String completeLogin(String body) {
        Document authenticationForm1 = Jsoup.parse(body);
        Element authenticationFormElement1 = authenticationForm1.select(ElementNames.FORM).first();

        HttpResponse authenticationResponse =
                createFormRequest(
                                authenticationFormElement1.attr(ElementAttributes.ACTION),
                                ElementUtils.parseFormParameters(authenticationFormElement1))
                        .post(HttpResponse.class);

        return LoginParsingUtils.getRedirectUrl(authenticationResponse.getBody(String.class));
    }

    public void signicatRedirect(String redirectUrl) {
        // Parse the JS window.location.href redirect from the Signicat Callback page, follow
        // that and submit the form to /signin-oidc-se
        HttpResponse authenticationFormResponse =
                client.request(Urls.IDENTITY_BASE_URL.concat(redirectUrl.substring(1)))
                        .accept(MediaType.APPLICATION_JSON)
                        .get(HttpResponse.class);

        Document authenticationForm = Jsoup.parse(authenticationFormResponse.getBody(String.class));
        Element authenticationFormElement = authenticationForm.select(ElementNames.FORM).first();

        createFormRequest(
                        authenticationFormElement.attr(ElementAttributes.ACTION),
                        ElementUtils.parseFormParameters(authenticationFormElement))
                .post(HttpResponse.class);
        // Try to access transaction page and verify that we aren't redirected
        LoginStatusResponse loggedInResponse =
                createRequest(Urls.LOGIN_STATUS_URL).get(LoginStatusResponse.class);

        if (!loggedInResponse.isAuthenticated()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private RequestBuilder createFormRequest(String url, MultivaluedMap<String, String> form) {
        String body = new Form(form).getBodyValue();

        return client.request(url)
                .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(HttpHeaders.ACCEPT_LANGUAGE, HeaderValues.LANGUAGE)
                .header(HttpHeaders.ACCEPT, HeaderValues.ACCEPT);
    }

    public SavingsAccountResponse fetchSavingsAccount() {
        return fetchAccountsRequest(Urls.SAVINGS_ACCOUNTS_URL, SavingsAccountResponse.class);
    }

    public CreditCardResponse fetchCardBalance() {
        return fetchAccountsRequest(Urls.CREDIT_CARD_URL, CreditCardResponse.class);
    }

    private <T> T fetchAccountsRequest(String url, Class<T> responseType) {
        HttpResponse response =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML)
                        .get(HttpResponse.class);

        if (response.getType().getType().equals(MediaType.APPLICATION_JSON_TYPE.getType())
                && response.getType()
                        .getSubtype()
                        .equals(MediaType.APPLICATION_JSON_TYPE.getSubtype())) {
            return response.getBody(responseType);
        }

        return null;
    }

    public CreditCardEntity fetchCardList() {
        final CreditCardOverviewResponse cardOverview =
                createRequest(Urls.CREDIT_CARD_OVERVIEW_URL).get(CreditCardOverviewResponse.class);

        return Optional.ofNullable(cardOverview.getCreditCardList()).orElse(Lists.newArrayList())
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<TransactionEntity> fetchTransactions(
            String accountNumber, Date fromDate, Date toDate) {
        SimpleDateFormat format = new SimpleDateFormat(NorwegianConstants.DATE_FORMAT);

        return fetchTransactions(
                accountNumber,
                format.format(fromDate),
                format.format(toDate),
                QueryValues.GET_LAST_DAYS_FALSE);
    }

    public List<TransactionEntity> fetchTransactions(
            String accountNumber, String from, String to, String getLastDays) {
        return Arrays.asList(
                createRequest(Urls.TRANSACTIONS_PAGINATION_URL)
                        .queryParam(QueryKeys.ACCOUNT_NUMBER, accountNumber)
                        .queryParam(QueryKeys.GET_LAST_DAYS, getLastDays)
                        .queryParam(QueryKeys.FROM_LAST_EOC, QueryValues.FROM_LAST_EOC)
                        .queryParam(QueryKeys.DATE_FROM, from)
                        .queryParam(QueryKeys.DATE_TO, to)
                        .queryParam(QueryKeys.CORE_DOWN, QueryValues.CORE_DOWN)
                        .get(TransactionEntity[].class));
    }

    public String fetchCreditCardAccountNumber() {
        return createRequest(Urls.CARD_TRANSACTION_URL)
                .get(AccountInfoResponse.class)
                .getAccountNo();
    }

    public String fetchSavingsAccountNumber() {
        return createRequest(Urls.SAVINGS_TRANSACTION_URL)
                .get(AccountInfoResponse.class)
                .getAccountNo();
    }

    public String fetchIdentityPage() {
        return createScrapeRequest(Urls.IDENTITY_URL).get(String.class);
    }
}
