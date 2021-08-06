package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.EBK;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.EBK2;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.EBK_SSO;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.GESTION;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.JSESSIONID;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.LOCATION;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.REFERER;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.REFERER_WEBSITE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.USER_ID;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls.BASE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls.IDENTITY_INFO;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls.IDENTITY_INIT_TRANSFER;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls.IDENTITY_TOKEN;

import com.google.api.client.http.HttpStatusCodes;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.rpc.CreditCardResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.identitydata.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.investment.rpc.InvestmentResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.loan.rpc.LoanResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.JsfUpdateResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BankinterApiClient {

    private final TinkHttpClient client;

    public BankinterApiClient(TinkHttpClient client) {
        this.client = client;
        client.setUserAgent(HeaderValues.USER_AGENT);
    }

    public boolean keepAlive() {
        try {
            final HttpResponse response = client.request(Urls.KEEP_ALIVE).get(HttpResponse.class);
            return HttpStatusCodes.isSuccess(response.getStatus());
        } catch (HttpResponseException hre) {
            return false;
        }
    }

    private Cookie convertCookie(org.openqa.selenium.Cookie cookie) {
        BasicClientCookie newCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
        newCookie.setDomain(cookie.getDomain());
        newCookie.setPath(cookie.getPath());
        newCookie.setExpiryDate(cookie.getExpiry());
        return newCookie;
    }

    public void storeLoginCookies(Set<org.openqa.selenium.Cookie> cookies) {
        cookies.stream().map(this::convertCookie).forEach(client::addCookie);
    }

    public IdentityDataResponse fetchIdentityData() {

        List<Cookie> cookies = client.getCookies();

        String ebk =
                cookies.stream()
                        .filter(cookie -> cookie.getName().equalsIgnoreCase(EBK))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("ebk cookie missing"))
                        .getValue();

        String jSessionId =
                cookies.stream()
                        .filter(
                                cookie ->
                                        cookie.getName().equalsIgnoreCase(JSESSIONID)
                                                && cookie.getPath().equalsIgnoreCase(GESTION))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("JSESSIONID cookie missing"))
                        .getValue();

        String ebkSso =
                cookies.stream()
                        .filter(cookie -> cookie.getName().equalsIgnoreCase(EBK_SSO))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("ebk_sso cookie missing"))
                        .getValue();

        HttpResponse initTransferResponse =
                client.request(IDENTITY_INIT_TRANSFER)
                        .header(REFERER, REFERER_WEBSITE)
                        .cookie(EBK, ebk)
                        .get(HttpResponse.class);

        String jsessionIdReceiveTicket =
                initTransferResponse.getCookies().stream()
                        .filter(cookie -> cookie.getName().contains(JSESSIONID))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "JSESSIONID cookie for receiving ticket  missing"))
                        .getValue();

        String ebk2ReceiveTicket =
                initTransferResponse.getCookies().stream()
                        .filter(cookie -> cookie.getName().contains(EBK2))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "ebk2 cookie for receiving ticket  missing"))
                        .getValue();

        String createTokenUrl = initTransferResponse.getHeaders().getFirst(LOCATION);

        HttpResponse createTokenResponse =
                client.request(createTokenUrl)
                        .header(REFERER, REFERER_WEBSITE)
                        .cookie(EBK, ebk)
                        .cookie(JSESSIONID, jSessionId)
                        .cookie(EBK_SSO, ebkSso)
                        .get(HttpResponse.class);

        String receiveTicketUrl = createTokenResponse.getHeaders().getFirst(LOCATION);

        HttpResponse receiveTicketResponse =
                client.request(receiveTicketUrl)
                        .header(REFERER, REFERER_WEBSITE)
                        .cookie(EBK, ebk)
                        .cookie(JSESSIONID, jsessionIdReceiveTicket)
                        .cookie(EBK2, ebk2ReceiveTicket)
                        .get(HttpResponse.class);

        String jSessionIdSecurity =
                receiveTicketResponse.getCookies().stream()
                        .filter(cookie -> cookie.getName().equalsIgnoreCase(JSESSIONID))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "JSESSIONID cookie for security missing"))
                        .getValue();

        HttpResponse tokenIdResponse =
                client.request(IDENTITY_TOKEN)
                        .header(REFERER, REFERER_WEBSITE)
                        .cookie(EBK, ebk)
                        .cookie(JSESSIONID, jSessionIdSecurity)
                        .get(HttpResponse.class);

        try {
            JSONObject info = new JSONObject(tokenIdResponse.getBody(String.class));
            String userId = info.getString(USER_ID);

            HttpResponse identityData =
                    client.request(IDENTITY_INFO + userId)
                            .header(REFERER, REFERER_WEBSITE)
                            .cookie(EBK, ebk)
                            .cookie(JSESSIONID, jSessionIdSecurity)
                            .get(HttpResponse.class);

            return SerializationUtils.deserializeFromString(
                    identityData.getBody(String.class), IdentityDataResponse.class);

        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    public GlobalPositionResponse fetchGlobalPosition() {
        return new GlobalPositionResponse(client.request(Urls.GLOBAL_POSITION).get(String.class));
    }

    public AccountResponse fetchAccount(String url) {
        return new AccountResponse(client.request(BASE + url).get(String.class));
    }

    public InvestmentResponse fetchInvestmentAccount(String url) {
        return new InvestmentResponse(client.request(BASE + url).get(String.class));
    }

    public CreditCardResponse fetchCreditCard(String url) {
        return new CreditCardResponse(client.request(BASE + url).get(String.class));
    }

    public LoanResponse fetchLoan(String url) {
        return new LoanResponse(client.request(BASE + url).get(String.class));
    }

    public LoanResponse fetchLoanPage(String source, String viewState, int offset) {
        final Form.Builder formBuilder = Form.builder();
        formBuilder.put(source, String.format("%d", offset));
        formBuilder.put(FormKeys.LOAN_TRANSACTIONS_SUBMIT, "1");
        formBuilder.put(FormKeys.JSF_VIEWSTATE, viewState);
        formBuilder.put(FormKeys.LOAN_TRANSACTIONS_ID, FormValues.LOAN_TRANSACTIONS_ID);

        return new LoanResponse(
                client.request(Urls.LOAN)
                        .body(
                                formBuilder.build().serialize(),
                                MediaType.APPLICATION_FORM_URLENCODED)
                        .post(String.class));
    }

    public <T extends JsfUpdateResponse> T fetchJsfUpdate(
            String url,
            String submitKey,
            String source,
            String viewState,
            Class<T> responseClass,
            String... render) {
        final Form.Builder formBuilder = Form.builder();
        formBuilder.put(submitKey, "1");
        formBuilder.put(FormKeys.JSF_VIEWSTATE, viewState);
        formBuilder.put(FormKeys.JSF_PARTIAL_AJAX, FormValues.TRUE);
        formBuilder.put(FormKeys.JSF_SOURCE, source);
        formBuilder.put(FormKeys.JSF_PARTIAL_EXECUTE, FormValues.JSF_EXECUTE_ALL);
        formBuilder.put(FormKeys.JSF_PARTIAL_RENDER, String.join(" ", render));
        formBuilder.put(source, source);

        final HttpResponse response =
                client.request(url)
                        .header(HeaderKeys.JSF_REQUEST, HeaderValues.JSF_PARTIAL)
                        .header(HeaderKeys.REQUESTED_WITH, HeaderValues.REQUESTED_WITH)
                        .body(
                                formBuilder.build().serialize(),
                                MediaType.APPLICATION_FORM_URLENCODED)
                        .post(HttpResponse.class);
        try {
            final Constructor<T> constructor = responseClass.getConstructor(String.class);
            return constructor.newInstance(response.getBody(String.class));
        } catch (InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Could not instantiate " + responseClass.getCanonicalName(), e);
        }
    }

    public JsfUpdateResponse fetchJsfUpdate(
            String url, String submitKey, String source, String viewState, String... render) {
        return fetchJsfUpdate(url, submitKey, source, viewState, JsfUpdateResponse.class, render);
    }
}
