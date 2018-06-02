package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken;

import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.Tags;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.FinalizeBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.FirstLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.SecondLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.SendSmsRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.VerifyCustomerResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.rpc.AccountFetchingResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class HandelsbankenNOApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public HandelsbankenNOApiClient(TinkHttpClient client,
            SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    private RequestBuilder requestInSession(URL url){
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                // at this moment, any random 8 numeric/alphabets works, but they might change later
                .header(HandelsbankenNOConstants.Header.REQUEST_ID, "11111111")
                .header(Headers.USER_AGENT)
                .header(Headers.X_EVRY_CLIENT)
                .header(HandelsbankenNOConstants.Header.EVRY_TOKEN, sessionStorage.get(Tags.ACCESS_TOKEN))
                .cookie(sessionStorage.get(Tags.SESSION_STAMP), sessionStorage.get(Tags.SESSION_STAMP_VALUE))
                .cookie(Tags.NONCE, sessionStorage.get(Tags.NONCE));
    }

    // === Authentication API calls ===
    public void fetchAppInformation() {
        client.request(Url.APP_INFORMATION.get())
                .accept(MediaType.WILDCARD)
                .get(HttpResponse.class);
    }

    public VerifyCustomerResponse verifyCustomer(String nationalId, String mobileNumber) {
        return client.request(Url.VERIFY_CUSTOMER.parameters(nationalId, mobileNumber))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON)
                .get(VerifyCustomerResponse.class);
    }

    public void configureBankId(String nationalId, String mobileNumber) {
        HttpResponse response = client.request(Url.CONFIGURE_BANKID.parameters(nationalId, mobileNumber))
                .accept(MediaType.TEXT_HTML_TYPE)
                .accept(MediaType.APPLICATION_XHTML_XML_TYPE)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .get(HttpResponse.class);

        String jSession = client.getCookies().stream()
                .filter(cookie -> cookie.getName().equalsIgnoreCase(Tags.JSESSION_ID))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new IllegalStateException("JSessionID is not found during authentication"));

        sessionStorage.put(Tags.JSESSION_ID, jSession);

    }

    public String initBankId(InitBankIdRequest initBankIdRequest) {
        URL url = Url.BANKID_1.parameters(sessionStorage.get(Tags.JSESSION_ID));

        return client.request(url)
                .header(Headers.ORIGIN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, initBankIdRequest);
    }

    public PollBankIdResponse pollBankId() {
        URL url = Url.POLL_BANK.parameters(sessionStorage.get(Tags.JSESSION_ID));

        return client.request(url)
                .accept(MediaType.WILDCARD)
                .header(Headers.ORIGIN)
                .header(Headers.REQUEST_WITH)
                .post(PollBankIdResponse.class);
    }

    public String finalizeBankId(FinalizeBankIdRequest finalizeBankIdRequest) {
        URL url = Url.BANKID_2.parameters(sessionStorage.get(Tags.JSESSION_ID));

        return client.request(url)
                .header(Headers.ORIGIN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, finalizeBankIdRequest);
    }

    public FirstLoginResponse loginFirstStep(String firstLoginRequest) {
        FirstLoginResponse firstLoginResponse = client.request(Url.LOGIN_FIRST_STEP.get())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(Headers.USER_AGENT)
                .header(Headers.X_EVRY_CLIENT)
                .post(FirstLoginResponse.class, firstLoginRequest);

        List<Cookie> cookies = client.getCookies();

        String nonce = cookies.stream()
                .filter(cookie -> cookie.getName().equalsIgnoreCase(Tags.NONCE))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new IllegalStateException("SECESB_NONCE is not found during authentication"));

        Cookie sessionStampCookie = cookies.stream()
                .filter(cookie -> cookie.getName().contains(HandelsbankenNOConstants.Tags.SESSION_STAMP))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("SESSSION_STAMP is not found during authentication"));

        sessionStorage.put(HandelsbankenNOConstants.Tags.NONCE, nonce);
        sessionStorage.put(HandelsbankenNOConstants.Tags.SESSION_STAMP, sessionStampCookie.getName());
        sessionStorage.put(HandelsbankenNOConstants.Tags.SESSION_STAMP_VALUE, sessionStampCookie.getValue());
        return firstLoginResponse;
    }

    public SecondLoginResponse loginSecondStep() {
        return client.request(Url.LOGIN_SECOND_STEP.get())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(Headers.USER_AGENT)
                .header(Headers.X_EVRY_CLIENT)
                .header(HandelsbankenNOConstants.Header.EVRY_TOKEN, sessionStorage.get(Tags.ACCESS_TOKEN))
                .post(SecondLoginResponse.class);
    }

    public HttpResponse sendSms(SendSmsRequest sendSmsRequest) {
        return requestInSession(Url.SEND_SMS.get())
                .post(HttpResponse.class, sendSmsRequest);
    }

    public AccountFetchingResponse fetchAccounts() {
        return requestInSession(Url.ACCOUNTS.get())
                .get(AccountFetchingResponse.class);
    }

    public HttpResponse fetchTransactions(String uri, int number, int index) {
        URL url = Url.TRANSACTIONS.parameters(uri, String.valueOf(number), String.valueOf(index));
        return requestInSession(url)
                .get(HttpResponse.class);
    }

    public HttpResponse extendSession() {
        return requestInSession(Url.KEEP_ALIVE.get())
                .get(HttpResponse.class);
    }
}
