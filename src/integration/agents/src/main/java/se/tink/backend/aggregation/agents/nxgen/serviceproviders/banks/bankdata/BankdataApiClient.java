package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.CompleteEnrollResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.DataRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.EncryptedResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.LoginInstalledRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.NemIdEnrollmentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.NemIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.NemIdLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataDepositEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataPoolAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.AssetDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.AssetDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.DepositsContentListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetDepositsContentListRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetDepositsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.PoolAccountsResponse;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.NemIdParameters;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BankdataApiClient {

    private final TinkHttpClient client;
    private final String bankdataBankNumber;

    private String actionPath;

    private final String ga = "GA1.2.39989436.1571391364";
    private final String gid = "GA1.2.389189714.1574003824";

    public BankdataApiClient(TinkHttpClient client, Provider provider) {
        this.client = client;
        this.bankdataBankNumber = provider.getPayload();
    }

    private RequestBuilder createJsonRequest(URL url) {
        return this.createRequest(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON_TYPE);
    }

    public GetAccountsResponse getAccounts() {
        return createRequest(BankdataConstants.Url.ACCOUNTS).get(GetAccountsResponse.class);
    }

    public GetTransactionsResponse getTransactions(GetTransactionsRequest getTransactionsRequest) {
        return createRequest(BankdataConstants.Url.PFM_TRANSACTIONS)
                .post(GetTransactionsResponse.class, getTransactionsRequest);
    }

    public GetTransactionsResponse getFutureTransactions(
            GetTransactionsRequest getTransactionsRequest) {
        return createRequest(BankdataConstants.Url.PFM_TRANSACTIONS_FUTURE)
                .post(GetTransactionsResponse.class, getTransactionsRequest);
    }

    public List<BankdataDepositEntity> fetchDeposits() {
        return createRequest(BankdataConstants.Url.DEPOSITS)
                .get(GetDepositsResponse.class)
                .getDeposits();
    }

    public List<BankdataPoolAccountEntity> fetchPoolAccounts() {
        return createRequest(BankdataConstants.Url.INVESTMENT_POOL_ACCOUNTS)
                .get(PoolAccountsResponse.class)
                .getPoolAccounts();
    }

    public DepositsContentListResponse fetchDepositContents(String regNo, String depositNo) {
        GetDepositsContentListRequest request =
                new GetDepositsContentListRequest().setDepositNo(depositNo).setRegNo(regNo);
        return createRequest(BankdataConstants.Url.DEPOSITS_CONTENT_LIST)
                .post(DepositsContentListResponse.class, request);
    }

    public AssetDetailsResponse fetchAssetDetails(
            int assetType, String depositRegNumber, String depositNumber, String securityId) {
        AssetDetailsRequest request =
                new AssetDetailsRequest()
                        .setAssetType(String.valueOf(assetType))
                        .setDepositRegNo(depositRegNumber)
                        .setDepositNo(depositNumber)
                        .setSecurityId(securityId);

        return createRequest(BankdataConstants.Url.ASSET_DETAILS)
                .post(AssetDetailsResponse.class, request);
    }

    private RequestBuilder createRequest(URL url) {
        return createRequest(url, MediaType.APPLICATION_JSON_TYPE);
    }

    private boolean isNotMobileCookie(Cookie cookie) {
        return !Objects.equals(cookie.getName(), "mobile");
    }

    private RequestBuilder createRequest(URL url, MediaType type) {
        cookieOverride();

        return client.request(url)
                .type(type)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.USER_AGENT, "iPhone, iOS, 12.4")
                .header(
                        BankdataConstants.Headers.X_VERSION,
                        BankdataConstants.Headers.X_VERSION_VALUE)
                .header(BankdataConstants.Headers.X_APPID, BankdataConstants.Headers.X_APPID_VALUE)
                .header(BankdataConstants.Headers.X_BANK_NO, bankdataBankNumber)
                .header("x-ios-device-model-id", "iPhone10,4")
                .header("x-ios-version", "12.4")
                .header("Accept-Charset", "utf-8")
                .header("Accept-Language", "da")
                .header("Accept-Encoding", "gzip")
                .header("Connection", "keep-alive")
                .cookie("_ga", ga)
                .cookie("_gid", gid);
    }

    public HttpResponse eventDoContinue(String token) {
        Form form =
                Form.builder().put("eventSubmit_doContinue", "Send").put("response", token).build();

        URL url =
                new URL(
                        "https://mobil.bankdata.dk/wps/portal/almbrand-dk/!ut/p/z1/04_Sj9CPykssy0xPLMnMz0vMAfIjo8ziPS1NTAw9DQw9LAycXAwcjd1M3SyC_YwM3M30w8EKgs2DXTzDPC0MDE1CHZ0C3EPNzIwMwEA_ihz9gb7GBqToN8ABHInUj0dBFH7jw_WjwErwhQBWBcheJGRJQW5oaGiEQaano6IiAHrmNso!/dz/d5/L2dBISEvZ0FBIS9nQSEh/p0/IZ7_79M422G0N82EB0QS3MJBS430G6=CZ6_I9441I01H80BD0A3F5F8SN20G6=LA0=Ejavax.portlet.action!login==/");

        return createRequest(url, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(HttpResponse.class, form.serialize());
    }

    public NemIdLoginResponse nemIdInit(final CryptoHelper cryptoHelper) {

        RSAPublicKey bankdataPublicKey =
                RSA.getPubKeyFromBytes(Base64.decodeBase64(BankdataConstants.Crypto.CERTIFICATE));

        // Encrypt our generated session key with their public key.
        // They will then use our session key for symmetric encryption.
        String data = escapeB64Data(cryptoHelper.getEncryptedSessionKey(bankdataPublicKey));
        NemIdInitRequest request = new NemIdInitRequest(data, BankdataConstants.Crypto.RSA_LABEL);

        return createRequest(BankdataConstants.Url.NEMID_INIT, MediaType.APPLICATION_JSON_TYPE)
                .post(EncryptedResponse.class, request)
                .decrypt(cryptoHelper, NemIdLoginResponse.class);
    }

    // TODO: Refactor
    public HttpResponse portal() {
        cookieOverride();
        HttpResponse httpResponse =
                client.request(
                                new URL(
                                        "https://mobil.bankdata.dk/wps/portal/almbrand-dk/mobilnemid"))
                        .header(
                                HttpHeaders.USER_AGENT,
                                "AlmBrandMobilBank/15773 CFNetwork/978.0.7 Darwin/18.7.0")
                        .header("Accept", "*/*")
                        .header("Accept-Language", "en-us")
                        .header("Accept-Encoding", "br, gzip, deflate")
                        .header("Connection", "keep-alive")
                        .get(HttpResponse.class);
        return httpResponse;
    }

    public NemIdParameters fetchNemIdParameters(HttpResponse httpResponse) {
        if (!httpResponse.hasBody()) {
            // todo: Handle this, it should have a body!
        }

        // We need the url
        MultivaluedMap<String, String> headers = httpResponse.getHeaders();
        String nemidUrlString = String.valueOf(headers.get("Content-Location"));
        // fixme: Find a cleaner way of removing the square brackets and the first slash
        // nemidUrlString = nemidUrlString.replaceFirst("\\[/", "").replaceFirst("\\]", "");

        // todo: Before doing the enrollment thing I have to deal with nemid via phantomjs/selenium
        // stuff
        String responseString = httpResponse.getBody(String.class);
        Document responseBody = Jsoup.parse(responseString);
        String launcher =
                responseBody
                        .getElementById("nemid_iframe")
                        .getElementsByAttribute("src")
                        .attr("src");

        String iframeTemplate =
                "<iframe id=\"nemid_iframe\" allowTransparency=\"true\" name=\"nemid_iframe\" scrolling=\"no\" style=\"z-index: 100; position: relative; width: 275px; height: 350px; border: 0\" src=\"%s\"></iframe>";
        String formattedIframe = String.format(iframeTemplate, launcher);

        String nemidParametersScriptTag =
                responseBody.getElementById("nemid_parameters").toString();
        String nemidParametersElement = nemidParametersScriptTag + formattedIframe;

        // URL nemidUrl = new URL(BankdataConstants.Url.BASE_URL + nemidUrlString);

        // get action path
        actionPath = responseBody.getElementsByTag("form").attr("action");

        NemIdParameters nemIdParameters = new NemIdParameters(nemidParametersElement);

        return nemIdParameters;
    }

    public CompleteEnrollResponse completeEnrollment(final CryptoHelper cryptoHelper) {

        String data = escapeB64Data(cryptoHelper.enrollCrypt());

        NemIdEnrollmentRequest request = new NemIdEnrollmentRequest(data);
        return createJsonRequest(
                        new URL("https://mobil.bankdata.dk/mobilbank/nemid/complete_enroll"))
                .post(EncryptedResponse.class, request)
                .decrypt(cryptoHelper, CompleteEnrollResponse.class);
    }

    public void loginWithInstallId(
            final String userId,
            final String pinCode,
            final String installId,
            final CryptoHelper cryptoHelper) {

        // TODO: Hardcoded pin/userid here. Removed in order not to commit.
        LoginInstalledRequest installedEntity =
                new LoginInstalledRequest("3010921081", "756075", installId);

        byte[] b64Entity = SerializationUtils.serializeToString(installedEntity).getBytes();
        String encrypted = escapeB64Data(cryptoHelper.encrypt(b64Entity));

        String response =
                createRequest(
                                new URL(
                                        "https://mobil.bankdata.dk/mobilbank/nemid/login_with_installid"))
                        .post(EncryptedResponse.class, new DataRequest(encrypted))
                        .decrypt(cryptoHelper);

        System.out.println(response);
    }

    // TODO: Bankdata has some formatting that we don't get with our lib.
    // TODO: There has to be a better way to do this.
    private String escapeB64Data(final String data) {

        String dataWithBackslash = data.replace("/", "\\/");
        //        return dataWithBackslash;
        return "\"" + dataWithBackslash + "\"";
    }

    private org.apache.http.cookie.Cookie createCookieForDomain(
            String name, String value, String domain) {
        org.apache.http.impl.cookie.BasicClientCookie newCookie =
                new org.apache.http.impl.cookie.BasicClientCookie(name, value);
        newCookie.setDomain(domain);
        newCookie.setPath("/");
        newCookie.setSecure(true);

        return newCookie;
    }

    private void cookieOverride() {
        List<Cookie> cookies =
                client.getCookies().stream()
                        .filter(this::isNotMobileCookie)
                        .collect(Collectors.toList());

        cookies.add(createCookieForDomain("mobile", "350|275|MobileBank", "mobil.bankdata.dk"));
        client.clearCookies();
        cookies.forEach(client::addCookie);
    }
}
