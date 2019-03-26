package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities.ExtensionsForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities.ExtensionsOtpLoginForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities.GenerateBindingFinalActionForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities.GenerateBindingQuickIdActionForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities.GenerateBindingSkipActionForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities.GenerateBindingTouchIdActionForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities.LoginOtpForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities.LoginUserNamePasswordForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities.MainForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities.MobileDevicesRequest;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher.entities.MainFetchTransactionsForDatesActionForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher.entities.MainFetchTransactionsForDatesChangeForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher.entities.MainFetchTransactionsGeneralCustomForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher.entities.MainSelectAccountForm;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class VolksbankApiClient {

    private static final Pattern SAVE_BINDING =
            Pattern.compile(
                    "GeraeteBindung\\.saveGeraeteBindung\\('(?<app>.*?)','(?<mandant>.*?)','(?<userId>.*?)','(?<secret>.*?)'\\)");
    private final TinkHttpClient apiclient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public VolksbankApiClient(
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            TinkHttpClient apiclient) {
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.apiclient = apiclient;
    }

    public static VolksbankApiClient create(
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            TinkHttpClient client) {
        return new VolksbankApiClient(persistentStorage, sessionStorage, client);
    }

    public void getLogin() {
        HttpResponse getLoginResponse =
                constructGetRequest(VolksbankConstants.Url.LOGIN)
                        .queryParam(
                                VolksbankConstants.QueryParam.M_KEY,
                                VolksbankConstants.QueryParam.M_VALUE)
                        .queryParam(
                                VolksbankConstants.QueryParam.A_KEY,
                                VolksbankConstants.QueryParam.A_VALUE)
                        .get(HttpResponse.class);
        extractViewState(getLoginResponse);
    }

    public void getLoginKeepSession() {
        HttpResponse getLoginKeepSessionResponse =
                constructGetRequest(VolksbankConstants.Url.LOGIN)
                        .queryParam(
                                VolksbankConstants.QueryParam.QUICK_KEY,
                                VolksbankConstants.QueryParam.QUICK_VALUE)
                        .queryParam(
                                VolksbankConstants.QueryParam.KEEPSESSION_KEY,
                                VolksbankConstants.QueryParam.KEEPSESSION_VALUE)
                        .get(HttpResponse.class);

        extractViewState(getLoginKeepSessionResponse);
    }

    public void getLoginUpgradeKeepSession() {
        HttpResponse getLoginUpgradeResponse =
                constructGetRequest(VolksbankConstants.Url.LOGIN)
                        .queryParam(
                                VolksbankConstants.QueryParam.UPGRADE_KEY, VolksbankConstants.TRUE)
                        .queryParam(
                                VolksbankConstants.QueryParam.KEEPSESSION_KEY,
                                VolksbankConstants.QueryParam.KEEPSESSION_VALUE)
                        .get(HttpResponse.class);

        extractViewState(getLoginUpgradeResponse);
    }

    public void postLoginOtp() {
        HttpResponse postLoginOtpResponse =
                constructPostRequest(
                                VolksbankConstants.Url.LOGIN,
                                new URL(VolksbankConstants.Url.LOGIN)
                                        .queryParam(
                                                VolksbankConstants.QueryParam.QUICK_KEY,
                                                VolksbankConstants.QueryParam.QUICK_VALUE)
                                        .queryParam(
                                                VolksbankConstants.QueryParam.KEEPSESSION_KEY,
                                                VolksbankConstants.QueryParam.KEEPSESSION_VALUE)
                                        .toString())
                        .body(
                                new LoginOtpForm(
                                        sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE)))
                        .post(HttpResponse.class);
        extractViewState(postLoginOtpResponse);
    }

    public void postLoginOtp(String userId, String generateId, String secret) {
        constructPostRequest(
                        VolksbankConstants.Url.LOGIN,
                        new URL(VolksbankConstants.Url.LOGIN)
                                .queryParam(
                                        VolksbankConstants.QueryParam.QUICK_KEY,
                                        VolksbankConstants.QueryParam.QUICK_VALUE)
                                .queryParam(
                                        VolksbankConstants.QueryParam.KEEPSESSION_KEY,
                                        VolksbankConstants.QueryParam.KEEPSESSION_VALUE)
                                .toString())
                .body(
                        new LoginOtpForm(
                                sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE),
                                userId,
                                generateId,
                                VolksbankCryptoHelper.getTotp(secret)))
                .post(HttpResponse.class);
    }

    public String postLoginUserNamePassword(String userId, String userName, String password) {
        HttpResponse response =
                constructPostRequest(
                                VolksbankConstants.Url.LOGIN,
                                new URL(VolksbankConstants.Url.LOGIN)
                                        .queryParam(
                                                VolksbankConstants.QueryParam.QUICK_KEY,
                                                VolksbankConstants.QueryParam.QUICK_VALUE)
                                        .queryParam(
                                                VolksbankConstants.QueryParam.KEEPSESSION_KEY,
                                                VolksbankConstants.QueryParam.KEEPSESSION_VALUE)
                                        .toString())
                        .body(
                                new LoginUserNamePasswordForm(
                                        userId,
                                        userName,
                                        VolksbankCryptoHelper.encryptPin(password),
                                        sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE)))
                        .post(HttpResponse.class);
        return this.getMessageFallback(response);
    }

    public void postLoginUserNamePasswordWithGid(
            String generateId, String userId, String userName, String password) {
        constructPostRequest(
                        VolksbankConstants.Url.LOGIN,
                        new URL(VolksbankConstants.Url.LOGIN)
                                .queryParam(
                                        VolksbankConstants.QueryParam.QUICK_KEY,
                                        VolksbankConstants.QueryParam.QUICK_VALUE)
                                .queryParam(
                                        VolksbankConstants.QueryParam.KEEPSESSION_KEY,
                                        VolksbankConstants.QueryParam.KEEPSESSION_VALUE)
                                .toString())
                .body(
                        new LoginUserNamePasswordForm(
                                generateId,
                                userId,
                                userName,
                                VolksbankCryptoHelper.encryptPin(password),
                                sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE)))
                .post(HttpResponse.class);
    }

    public void postExtensions() {
        constructPostRequest(VolksbankConstants.Url.EXTENSIONS, VolksbankConstants.Url.EXTENSIONS)
                .header(
                        VolksbankConstants.Header.ORIGIN_KEY,
                        VolksbankConstants.Header.ORIGIN_VALUE)
                .body(new ExtensionsForm(sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE)))
                .post(HttpResponse.class);
    }

    public void postExtensionsOtpLogin() {
        HttpResponse postExtensionsOtpLoginResponse =
                constructPostRequest(
                                VolksbankConstants.Url.EXTENSIONS,
                                VolksbankConstants.Url.EXTENSIONS)
                        .body(
                                new ExtensionsOtpLoginForm(
                                        sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE)))
                        .post(HttpResponse.class);
        extractViewState(postExtensionsOtpLoginResponse);
    }

    public void postExtensionsOtpLogin(String userId, String generateId, String secret) {
        HttpResponse postExtensionsOtpLoginResponse =
                constructPostRequest(
                                VolksbankConstants.Url.EXTENSIONS,
                                VolksbankConstants.Url.EXTENSIONS)
                        .header(
                                VolksbankConstants.Header.ORIGIN_KEY,
                                VolksbankConstants.Header.ORIGIN_VALUE)
                        .body(
                                new ExtensionsOtpLoginForm(
                                        sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE),
                                        userId,
                                        generateId,
                                        VolksbankCryptoHelper.getTotp(secret)))
                        .post(HttpResponse.class);
        extractViewState(postExtensionsOtpLoginResponse);
    }

    public void getGenerateBinding() {
        HttpResponse getGenerateConnResponse =
                constructGetRequest(VolksbankConstants.Url.GENERATE_BINDING)
                        .header(
                                VolksbankConstants.Header.REFERER_KEY,
                                new URL(VolksbankConstants.Url.LOGIN)
                                        .queryParam(
                                                VolksbankConstants.QueryParam.QUICK_KEY,
                                                VolksbankConstants.QueryParam.QUICK_VALUE)
                                        .queryParam(
                                                VolksbankConstants.QueryParam.KEEPSESSION_KEY,
                                                VolksbankConstants.QueryParam.KEEPSESSION_VALUE)
                                        .toString())
                        .get(HttpResponse.class);
        extractViewState(getGenerateConnResponse);
    }

    public void postGenerateBindingQuickIdAction() {
        HttpResponse postGenerateBindingQuickIdAction =
                constructPostRequest(
                                VolksbankConstants.Url.GENERATE_BINDING,
                                VolksbankConstants.Url.GENERATE_BINDING)
                        .body(
                                new GenerateBindingQuickIdActionForm(
                                        sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE)))
                        .post(HttpResponse.class);

        extractViewState(postGenerateBindingQuickIdAction);
    }

    public void postGenerateBindingTouchIdAction() {
        HttpResponse postGenerateBindingTouchIdAction =
                constructPostRequest(
                                VolksbankConstants.Url.GENERATE_BINDING,
                                VolksbankConstants.Url.GENERATE_BINDING)
                        .body(
                                new GenerateBindingTouchIdActionForm(
                                        sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE)))
                        .post(HttpResponse.class);

        extractViewState(postGenerateBindingTouchIdAction);

        Document binding =
                Jsoup.parse(
                        postGenerateBindingTouchIdAction.getBody(String.class),
                        VolksbankConstants.UTF_8);
        String formText = binding.getElementById(VolksbankConstants.Body.GBFORM_ELEMENT_ID).text();
        Matcher matcher = SAVE_BINDING.matcher(formText);
        if (matcher.find()) {
            String secret = matcher.group("secret");
            persistentStorage.put(VolksbankConstants.Storage.SECRET, secret);
        } else {
            throw new IllegalStateException("No binding found, or the format is changed");
        }
    }

    public void postGenerateBindingFinalAction() {
        String deviceId = UUID.randomUUID().toString().toUpperCase();
        String secret = persistentStorage.get(VolksbankConstants.Storage.SECRET);
        String pushToken = VolksbankCryptoHelper.generateRandomHex().toLowerCase();
        sessionStorage.put(VolksbankConstants.Storage.PUSHTOKEN, pushToken);
        persistentStorage.put(VolksbankConstants.Storage.GENERATE_ID, deviceId);

        constructPostRequest(
                        VolksbankConstants.Url.GENERATE_BINDING,
                        VolksbankConstants.Url.GENERATE_BINDING)
                .body(
                        new GenerateBindingFinalActionForm(
                                sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE),
                                deviceId,
                                secret,
                                pushToken))
                .post(HttpResponse.class);
    }

    public void postGenerateBindingSkipAction() {
        constructPostRequest(
                        VolksbankConstants.Url.GENERATE_BINDING,
                        VolksbankConstants.Url.GENERATE_BINDING)
                .body(
                        new GenerateBindingSkipActionForm(
                                sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE)))
                .post(HttpResponse.class);
    }

    public void postMobileDevices() {
        MobileDevicesRequest request = new MobileDevicesRequest();
        request.setSetupId(persistentStorage.get(VolksbankConstants.Storage.GENERATE_ID));
        request.setSetupName(VolksbankConstants.Form.SECRET_NAME_VALUE);
        request.setSetupOs("iOS");
        request.setPushToken(sessionStorage.get(VolksbankConstants.Storage.PUSHTOKEN));

        constructPostRequest(VolksbankConstants.Url.MOBILEDEVICES, VolksbankConstants.Url.DASHBOARD)
                .header(
                        VolksbankConstants.Header.ORIGIN_KEY,
                        VolksbankConstants.Header.ORIGIN_VALUE)
                .type(VolksbankConstants.Header.TYPE_JSON)
                .post(HttpResponse.class, request);
    }

    // Mobile Auth is not a mandadory steps for now
    //    public void getMobileAuth() {
    //
    //        HttpResponse getMobileAuthResponse =
    // constructGetRequest(VolksbankConstants.Url.MOBILEAUTH)
    //                .header(VolksbankConstants.Header.REFERER_KEY, new
    // URL(VolksbankConstants.Url.LOGIN)
    //                        .queryParam(VolksbankConstants.QueryParam.QUICK_KEY,
    // VolksbankConstants.QueryParam.QUICK_VALUE)
    //                        .queryParam(VolksbankConstants.QueryParam.KEEPSESSION_KEY,
    //                                VolksbankConstants.QueryParam.KEEPSESSION_VALUE).toString())
    //                .get(HttpResponse.class);
    //        extractViewState(getMobileAuthResponse);
    //    }
    //
    //    public void postMobileAuth() {
    //        constructPostRequest(VolksbankConstants.Url.MOBILEAUTH,
    // VolksbankConstants.Url.MOBILEAUTH)
    //                .body(new MobileAuthForm(this.viewState))
    //                .post();
    //    }

    public HttpResponse getMain() {
        HttpResponse getMainResponse =
                constructGetRequest(VolksbankConstants.Url.MAIN)
                        .header(
                                VolksbankConstants.Header.REFERER_KEY,
                                VolksbankConstants.Url.GENERATE_BINDING)
                        .get(HttpResponse.class);
        extractViewState(getMainResponse);
        return getMainResponse;
    }

    public HttpResponse postMain() {
        HttpResponse postMainResposne =
                constructPostRequest(VolksbankConstants.Url.MAIN, VolksbankConstants.Url.DASHBOARD)
                        .body(
                                new MainForm(
                                        sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE)))
                        .post(HttpResponse.class);
        extractViewState(postMainResposne);
        return postMainResposne;
    }

    public void postMainSelectAccount(String accountId) {
        List<String> productIds =
                sessionStorage
                        .get(
                                VolksbankConstants.Storage.PRODUCT_ID,
                                new TypeReference<List<String>>() {})
                        .get();

        String commonId = accountId.substring(accountId.length() - 8);

        String targetProductId =
                productIds.stream()
                        .filter(productId -> productId.contains(commonId))
                        .findFirst()
                        .orElseThrow(IllegalStateException::new);

        HttpResponse postMainSelectAccountResponse =
                constructPostRequest(VolksbankConstants.Url.MAIN, VolksbankConstants.Url.DASHBOARD)
                        .body(
                                new MainSelectAccountForm(
                                        sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE),
                                        targetProductId))
                        .post(HttpResponse.class);
        extractViewState(postMainSelectAccountResponse);
    }

    public void postMainFetchTransactionGeneralCustom() {
        HttpResponse response =
                constructPostRequest(VolksbankConstants.Url.MAIN, VolksbankConstants.Url.GIROKONTO)
                        .body(
                                new MainFetchTransactionsGeneralCustomForm(
                                        sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE)))
                        .post(HttpResponse.class);
        extractViewState(response);
    }

    public void postMainFetchTransactionForDateChange(Date start, Date end) {
        HttpResponse response =
                constructPostRequest(VolksbankConstants.Url.MAIN, VolksbankConstants.Url.GIROKONTO)
                        .body(
                                new MainFetchTransactionsForDatesChangeForm(
                                        sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE),
                                        start,
                                        end))
                        .post(HttpResponse.class);
        extractViewState(response);
    }

    public HttpResponse postMainFetchTransactionForDateAction(Date start, Date end) {
        HttpResponse postFetchTransactionForDataResponse =
                constructPostRequest(VolksbankConstants.Url.MAIN, VolksbankConstants.Url.GIROKONTO)
                        .body(
                                new MainFetchTransactionsForDatesActionForm(
                                        sessionStorage.get(VolksbankConstants.Storage.VIEWSTATE),
                                        start,
                                        end))
                        .post(HttpResponse.class);
        extractViewState(postFetchTransactionForDataResponse);
        return postFetchTransactionForDataResponse;
    }

    public void logout() {
        constructGetRequest(VolksbankConstants.Url.LOGOUT)
                .header(VolksbankConstants.Header.REFERER_KEY, VolksbankConstants.Url.DASHBOARD)
                .get(HttpResponse.class);
    }

    private void extractViewState(HttpResponse response) {
        Element viewStateElement =
                Jsoup.parse(response.getBody(String.class), VolksbankConstants.UTF_8)
                        .getElementById(VolksbankConstants.Body.VIEW_STATE_ID);

        if (viewStateElement.tagName().equals(VolksbankConstants.Body.INPUT_TAG)) {
            sessionStorage.put(VolksbankConstants.Storage.VIEWSTATE, viewStateElement.val());
        } else if (viewStateElement.tagName().equals(VolksbankConstants.Body.UPDATE_TAG)) {
            sessionStorage.put(VolksbankConstants.Storage.VIEWSTATE, viewStateElement.text());
        }
    }

    private RequestBuilder constructGetRequest(String url) {
        return apiclient
                .request(url)
                .accept(VolksbankConstants.Header.ACCEPT)
                .header(
                        VolksbankConstants.Header.ACCEPT_LANGUAGE_KEY,
                        VolksbankConstants.Header.ACCEPT_LANGUAGE_VALUE)
                .header(
                        VolksbankConstants.Header.ACCEPT_ENCODING_KEY,
                        VolksbankConstants.Header.ACCEPT_ENCODING_VALUE);
    }

    private RequestBuilder constructPostRequest(String url, String referer) {
        return this.constructGetRequest(url)
                .header(VolksbankConstants.Header.REFERER_KEY, referer)
                .type(VolksbankConstants.Header.CONTENT_TYPE_VALUE)
                .header(
                        VolksbankConstants.Header.FACES_REQUEST_KEY,
                        VolksbankConstants.Header.FACES_REQUEST_VALUE);
    }

    private String getMessageFallback(HttpResponse response) {
        Element messageFallback =
                Jsoup.parse(response.getBody(String.class), VolksbankConstants.UTF_8)
                        .getElementById(VolksbankConstants.Body.MESSAGE_FALLBACK_ID);
        return messageFallback == null ? "" : messageFallback.text();
    }
}
