package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria;

import com.sun.jersey.api.representation.Form;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.entities.OtmlResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;
import se.tink.backend.aggregation.utils.deviceprofile.entity.UserAgentEntity;
import se.tink.libraries.strings.StringUtils;

import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class BankAustriaApiClient {
    public static final String USER_ID = "userId";
    private final TinkHttpClient client;
    private final BankAustriaSessionStorage sessionStorage;

    public BankAustriaApiClient(TinkHttpClient client, BankAustriaSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }


    public OtmlResponse login(String userId, String password) {
        OtmlResponse response = getRequestWithHeaders(BankAustriaConstants.Urls.LOGIN)
                .post(OtmlResponse.class, loginForm(userId, password));
        return response;
    }

    public void logout() {
        getRequestWithHeaders(BankAustriaConstants.Urls.LOGOUT).post();
    }

    public void acceptRtaMessage(String messageId) {
        getRequestWithHeaders(BankAustriaConstants.Urls.RTA_MESSAGE).post(rtaForm(messageId));
    }

    private Form rtaForm(String messageId) {
        Form form = new Form();
        form.putSingle(BankAustriaConstants.RtaMessageForm.MESSAGE_ID, messageId);
        form.putSingle(BankAustriaConstants.RtaMessageForm.CANCEL, "false");
        form.putSingle(BankAustriaConstants.GenericForm.SOURCE, BankAustriaConstants.GenericForm.SOURCE_OTML);
        return form;
    }

    private Form loginForm(String userId, String password) {
        Form form = new Form();
        form.putSingle(USER_ID, userId);
        form.putSingle(BankAustriaConstants.LoginForm.PASSWORD, password);
        form.putSingle(BankAustriaConstants.LoginForm.OTML_SECURE_ENCLAVE_PARAMS, BankAustriaConstants.LoginForm.OTML_SECURE_ENCLAVE_TOKEN);
        form.putSingle(BankAustriaConstants.LoginForm.USE_LOGIN_VIA_FINGERPRINT, "");
        form.putSingle(BankAustriaConstants.LoginForm.ACTIVATE_FINGERPRINT_MESSAGE, BankAustriaConstants.LoginForm.ACTIVATE_FINGERPRINT_LOGIN_VALUE);
        form.putSingle(BankAustriaConstants.LoginForm.LANGUAGE, BankAustriaConstants.LoginForm.LANGUAGE_EN_US);
        form.putSingle(BankAustriaConstants.LoginForm.SUPPORT_FACE_ID, BankAustriaConstants.LoginForm.SUPPORT_FACE_ID_FALSE);
        form.putSingle(BankAustriaConstants.LoginForm.OPTPARAM, "");
        form.putSingle(BankAustriaConstants.LoginForm.SECURITY_CHECK_AVAILABLE, "");
        return form;
    }


    public OtmlResponse getAccountsFromSettings() {
        OtmlResponse response = getRequestWithHeaders(BankAustriaConstants.Urls.SETTINGS)
                .post(OtmlResponse.class, settingsForm());
        return response;
    }

    private Form settingsForm() {
        Form form = new Form();
        form.putSingle(BankAustriaConstants.SettingsForm.SETTINGS_TARGET, BankAustriaConstants.SettingsForm.SETTINGS_TARGET_VALUE);
        return form;
    }

    public OtmlResponse getAccountInformationFromAccountMovement(TransactionalAccount account) {
        URL url = getUrlForAccountType(account);

        OtmlResponse response = getRequestWithHeaders(url)
                .post(OtmlResponse.class, getFormForGenericAccount(account.getBankIdentifier()));

        return response;
    }

    public String getMD5OfUpdatePage() {
        String response = getRequestWithHeaders(BankAustriaConstants.Urls.UPDATE_PAGE)
                .post(String.class, null);
        return StringUtils.hashAsStringMD5(response).toUpperCase();
    }

    public OtmlResponse getTransactionsForDatePeriod(TransactionalAccount account, Date fromDate, Date toDate) {
        URL url = getUrlForAccountType(account);
        OtmlResponse response = getRequestWithHeaders(url)
                .post(OtmlResponse.class, getFormForDatePagination(account.getBankIdentifier(), fromDate, toDate));
        return response;
    }

    private URL getUrlForAccountType(TransactionalAccount account) {
        URL url = BankAustriaConstants.Urls.MOVEMENTS;
        if (account.getType().equals(AccountTypes.SAVINGS)) {
            url = BankAustriaConstants.Urls.SAVINGS_MOVEMENTS;
        }
        return url;
    }

    private String getResolution() {
        return new StringBuilder()
                .append("{")
                .append(DeviceProfileConfiguration.IOS_STABLE.getScreenWidth())
                .append(", ")
                .append(DeviceProfileConfiguration.IOS_STABLE.getScreenHeight())
                .append("}")
                .toString();
    }
    private String getDeviceId() {
        return new StringBuilder()
                .append(DeviceProfileConfiguration.IOS_STABLE.getMake()).append("_")
                .append(DeviceProfileConfiguration.IOS_STABLE.getModelNumber()).append("_")
                .append(DeviceProfileConfiguration.IOS_STABLE.getOs()).append("_")
                .append(DeviceProfileConfiguration.IOS_STABLE.getOsVersion())
                .toString();
    }
    private String getUserAgent() {
        UserAgentEntity useragent = DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity();
        useragent.setExtensions(BankAustriaConstants.Device.USERAGENT_EXTENSION);
        return useragent.toString();
    }

    private RequestBuilder getRequestWithHeaders(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(BankAustriaConstants.Header.X_OTML_CLUSTER, getResolution())
                .header(BankAustriaConstants.Header.X_DEVICE, getDeviceId())
                .header(BankAustriaConstants.Header.USER_AGENT, getUserAgent())
                .header(BankAustriaConstants.Header.X_OTML_MANIFEST, sessionStorage.getXOtmlManifest())
                .header(BankAustriaConstants.Header.X_OTML_PLATFORM, BankAustriaConstants.Application.PLATFORM)
                .header(BankAustriaConstants.Header.X_OTMLID, BankAustriaConstants.Application.OTMLID)
                .header(BankAustriaConstants.Header.X_APPID, BankAustriaConstants.Application.PLATFORM_VERSION);
    }

    private Form getFormForGenericAccount(String bankIdentifier) {
        Form form = new Form();
        form.putSingle(BankAustriaConstants.MovementsSearchForm.SEARCH_TYPE, BankAustriaConstants.MovementsSearchForm.SEARCH_TYPE_VALUE_LAST_30_DAYS);
        form.putSingle(BankAustriaConstants.MovementsSearchForm.ACCOUNT_ID, bankIdentifier);
        form.putSingle(BankAustriaConstants.MovementsSearchForm.START_YEAR, "");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.START_MONTH, "");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.START_DAY, "");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.END_YEAR, "");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.END_MONTH, "");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.END_DAY, "");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.TYPE, BankAustriaConstants.MovementsSearchForm.TYPE_ALL);
        form.putSingle(BankAustriaConstants.GenericForm.SOURCE, BankAustriaConstants.GenericForm.SOURCE_OTML);
        form.putSingle(BankAustriaConstants.MovementsSearchForm.AMOUNT_TO_POSITIVITY, "+");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.AMOUNT_FROM_POSITIVITY, "+");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.OTML_BIND_SIDE_CONTENT, "");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.FINISH, BankAustriaConstants.MovementsSearchForm.FINISH_VALUE);
        form.putSingle(BankAustriaConstants.MovementsSearchForm.OTML_FIRST_SEARCH, BankAustriaConstants.MovementsSearchForm.OTML_FIRST_SEARCH_VALUE_FALSE);
        form.putSingle(BankAustriaConstants.MovementsSearchForm.AMOUNT_FROM, "");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.AMOUNT_TO, "");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.TEXT, "");
        return form;
    }


    private Form getFormForDatePagination(String bankIdentifier, Date fromDate, Date toDate) {
        LocalDate localfromDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localtoDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Form form = new Form();
        form.putSingle(BankAustriaConstants.MovementsSearchForm.SEARCH_TYPE, BankAustriaConstants.MovementsSearchForm.SEARCH_TYPE_VALUE_CUSTOM);
        form.putSingle(BankAustriaConstants.MovementsSearchForm.ACCOUNT_ID, bankIdentifier);
        form.putSingle(BankAustriaConstants.MovementsSearchForm.START_YEAR, Integer.toString(localfromDate.getYear()));
        form.putSingle(BankAustriaConstants.MovementsSearchForm.START_MONTH, Integer.toString(localfromDate.getMonthValue()));
        form.putSingle(BankAustriaConstants.MovementsSearchForm.START_DAY, Integer.toString(localfromDate.getDayOfMonth()));
        form.putSingle(BankAustriaConstants.MovementsSearchForm.END_YEAR, Integer.toString(localtoDate.getYear()));
        form.putSingle(BankAustriaConstants.MovementsSearchForm.END_MONTH, Integer.toString(localtoDate.getMonthValue()));
        form.putSingle(BankAustriaConstants.MovementsSearchForm.END_DAY, Integer.toString(localtoDate.getDayOfMonth()));
        form.putSingle(BankAustriaConstants.MovementsSearchForm.TYPE, BankAustriaConstants.MovementsSearchForm.TYPE_ALL);
        form.putSingle(BankAustriaConstants.GenericForm.SOURCE, BankAustriaConstants.GenericForm.SOURCE_OTML);
        form.putSingle(BankAustriaConstants.MovementsSearchForm.AMOUNT_TO, "+");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.AMOUNT_FROM_POSITIVITY, "+");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.OTML_BIND_SIDE_CONTENT, "");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.FINISH, BankAustriaConstants.MovementsSearchForm.FINISH_VALUE);
        form.putSingle(BankAustriaConstants.MovementsSearchForm.OTML_FIRST_SEARCH, BankAustriaConstants.MovementsSearchForm.OTML_FIRST_SEARCH_VALUE_FALSE);
        form.putSingle(BankAustriaConstants.MovementsSearchForm.AMOUNT_FROM, "");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.AMOUNT_TO, "");
        form.putSingle(BankAustriaConstants.MovementsSearchForm.TEXT, "");
        return form;
    }
}
