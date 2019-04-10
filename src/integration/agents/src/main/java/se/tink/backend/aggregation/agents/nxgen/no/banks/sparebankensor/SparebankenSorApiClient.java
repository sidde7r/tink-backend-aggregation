package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor;

import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants.StaticUrlValuePairs;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants.StaticUrlValues;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants.UrlQueryParameters;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.FinalizeBankIdBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.FirstLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.FirstLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.InitBankIdBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.SecondLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.SendSmsRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.VerifyCustomerResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.loan.rpc.SoTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.rpc.TransactionListResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SparebankenSorApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private String jSessionId;
    private final SecureRandom secureRandom;

    private List<AccountEntity> accountList;

    public SparebankenSorApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.secureRandom = new SecureRandom();
    }

    public void fetchAppInformation() {
        client.request(Url.APP_INFORMATION).accept(MediaType.WILDCARD).get(HttpResponse.class);
    }

    public VerifyCustomerResponse verifyCustomer(String nationalId, String mobilenumber) {
        // There is a literal + sign in the url that Sor uses, so we can't use the parameter or
        // queryParam
        // functions here because they url encode the values.
        URL url =
                new URL(
                        Url.SECESB_IDENTIFY_CUSTOMER
                                + String.format(
                                        "%s/mobile?number=+47%s&orgid=%s",
                                        nationalId, mobilenumber, StaticUrlValues.ORG_ID));

        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(
                        SparebankenSorConstants.Headers.NAME_CLIENTNAME,
                        SparebankenSorConstants.Headers.VALUE_CLIENTNAME)
                .header(SparebankenSorConstants.Headers.NAME_REQUESTID, generateRequestId())
                .get(VerifyCustomerResponse.class);
    }

    public void configureBankId(String nationalId, String mobilenumber) {
        URL url =
                Url.CONFIGURE_BANKID
                        .queryParam(
                                StaticUrlValuePairs.CONFIG_KEY.getKey(),
                                StaticUrlValuePairs.CONFIG_KEY.getValue())
                        .queryParam(UrlQueryParameters.USER_ID, nationalId)
                        .queryParam(UrlQueryParameters.PHONE_NUMBER, mobilenumber);

        client.request(url).accept(MediaType.WILDCARD).get(HttpResponse.class);
    }

    public String initBankId(InitBankIdBody initBankIdBody) {
        URL url =
                new URL(Url.BANKID_MOBILE + jSessionId)
                        .queryParam(
                                StaticUrlValuePairs.INIT_BANKID.getKey(),
                                StaticUrlValuePairs.INIT_BANKID.getValue());

        return client.request(url)
                .header(SparebankenSorConstants.Headers.NAME_ORIGIN, Url.HOST)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, initBankIdBody);
    }

    public PollBankIdResponse pollBankId() {
        URL url = new URL(Url.POLL_BANKID + jSessionId);

        return client.request(url)
                .accept(MediaType.WILDCARD)
                .header(SparebankenSorConstants.Headers.NAME_ORIGIN, Url.HOST)
                .header(
                        SparebankenSorConstants.Headers.NAME_REQUESTED_WITH,
                        SparebankenSorConstants.Headers.VALUE_REQUESTED_WITH)
                .post(PollBankIdResponse.class);
    }

    public String finalizeBankId(FinalizeBankIdBody finalizeBankIdBody) {
        URL url =
                new URL(Url.BANKID_MOBILE + jSessionId)
                        .queryParam(
                                StaticUrlValuePairs.FINALIZE_BANKID.getKey(),
                                StaticUrlValuePairs.FINALIZE_BANKID.getValue());

        return client.request(url)
                .header(SparebankenSorConstants.Headers.NAME_ORIGIN, Url.HOST)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, finalizeBankIdBody);
    }

    public FirstLoginResponse loginFirstStep(FirstLoginRequest firstLoginRequest) {

        return client.request(Url.LOGIN_FIRST_STEP)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(
                        SparebankenSorConstants.Headers.NAME_CLIENTNAME,
                        SparebankenSorConstants.Headers.VALUE_CLIENTNAME)
                .header(SparebankenSorConstants.Headers.NAME_REQUESTID, generateRequestId())
                .post(FirstLoginResponse.class, firstLoginRequest);
    }

    public SecondLoginResponse loginSecondStep() {
        return getRequestWithCommonHeaders(Url.LOGIN_SECOND_STEP).post(SecondLoginResponse.class);
    }

    public HttpResponse sendSms(SendSmsRequest sendSmsRequest) {
        return getRequestWithCommonHeaders(Url.SEND_SMS).post(HttpResponse.class, sendSmsRequest);
    }

    public List<AccountEntity> fetchAccounts() {

        if (accountList != null && !accountList.isEmpty()) {
            return accountList;
        }

        AccountListResponse accountListResponse =
                getRequestWithCommonHeaders(Url.FETCH_ACCOUNTS).get(AccountListResponse.class);

        List<AccountEntity> accountList = accountListResponse.getAccountList();
        this.accountList = accountList;

        return accountList;
    }

    public TransactionListResponse fetchTransactions(String transactionsPath) {
        URL url =
                new URL(SparebankenSorConstants.Url.BASE_PATH + transactionsPath)
                        .queryParam(
                                StaticUrlValuePairs.TRANSACTIONS_BATCH_SIZE.getKey(),
                                StaticUrlValuePairs.TRANSACTIONS_BATCH_SIZE.getValue())
                        .queryParam(
                                StaticUrlValuePairs.RESERVED_TRANSACTIONS.getKey(),
                                StaticUrlValuePairs.RESERVED_TRANSACTIONS.getValue());

        return getRequestWithCommonHeaders(url).get(TransactionListResponse.class);
    }

    // just logging response
    public String fetchCreditCards() {
        return getRequestWithCommonHeaders(Url.FETCH_CREDIT_CARDS).get(String.class);
    }

    // For loan fetching
    public SoTokenResponse fetchSoToken(URL url) {
        return client.request(url)
                .header(SparebankenSorConstants.Headers.NAME_ORIGIN, Url.HOST)
                .header(
                        SparebankenSorConstants.Headers.NAME_CLIENTNAME,
                        SparebankenSorConstants.Headers.VALUE_CLIENTNAME)
                .header(SparebankenSorConstants.Headers.NAME_REQUESTID, generateRequestId())
                .accept(MediaType.WILDCARD)
                .header(
                        SparebankenSorConstants.Headers.NAME_ACCESSTOKEN,
                        sessionStorage.get(SparebankenSorConstants.Storage.ACCESS_TOKEN))
                .post(SoTokenResponse.class);
    }

    // For loan fetching
    public void transigoLogon(URL transigoLogonUrl) {
        client.request(transigoLogonUrl).accept(MediaType.WILDCARD).get(HttpResponse.class);
    }

    // Just logging response
    public String transigoAccounts(URL url) {
        return client.request(url).accept(MediaType.WILDCARD).get(String.class);
    }

    public String fetchLoanDetails(String detailsPath) {
        URL url = new URL(SparebankenSorConstants.Url.BASE_PATH + detailsPath);

        return getRequestWithCommonHeaders(url).get(String.class);
    }

    private RequestBuilder getRequestWithCommonHeaders(URL url) {

        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(
                        SparebankenSorConstants.Headers.NAME_CLIENTNAME,
                        SparebankenSorConstants.Headers.VALUE_CLIENTNAME)
                .header(SparebankenSorConstants.Headers.NAME_REQUESTID, generateRequestId())
                .header(
                        SparebankenSorConstants.Headers.NAME_ACCESSTOKEN,
                        sessionStorage.get(SparebankenSorConstants.Storage.ACCESS_TOKEN));
    }

    public void setSessionIdForBankIdUrls() {
        jSessionId =
                client.getCookies().stream()
                        .filter(
                                cookie ->
                                        Objects.equals(
                                                cookie.getName().toLowerCase(), "jsessionid"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "JSESSIONID cookie not found, is needed for bankId authentication."));
    }

    private String generateRequestId() {
        byte[] requestIdData = new byte[4];
        secureRandom.nextBytes(requestIdData);

        return Hex.encodeHexString(requestIdData).toUpperCase();
    }
}
