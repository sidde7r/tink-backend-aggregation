package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria;

import com.sun.jersey.api.representation.Form;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaConstants.Application;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaConstants.Device;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaConstants.GenericForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaConstants.Header;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaConstants.LoginForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaConstants.MovementsSearchForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaConstants.RtaMessageForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaConstants.SettingsForm;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.entities.OtmlResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;
import se.tink.backend.aggregation.utils.deviceprofile.entity.UserAgentEntity;
import se.tink.libraries.strings.StringUtils;

public class BankAustriaApiClient {
    private final TinkHttpClient client;
    private final BankAustriaSessionStorage sessionStorage;

    public BankAustriaApiClient(TinkHttpClient client, BankAustriaSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public OtmlResponse login(String userId, String password) {
        OtmlResponse response =
                getRequestWithHeaders(Urls.LOGIN)
                        .post(OtmlResponse.class, loginForm(userId, password));
        return response;
    }

    public void logout() {
        getRequestWithHeaders(Urls.LOGOUT).post();
    }

    public void acceptRtaMessage(String messageId) {
        getRequestWithHeaders(Urls.RTA_MESSAGE).post(rtaForm(messageId));
    }

    private Form rtaForm(String messageId) {
        Form form = new Form();
        form.putSingle(RtaMessageForm.MESSAGE_ID, messageId);
        form.putSingle(RtaMessageForm.CANCEL, "false");
        form.putSingle(GenericForm.SOURCE, GenericForm.SOURCE_OTML);
        return form;
    }

    private Form loginForm(String userId, String password) {
        Form form = new Form();
        form.putSingle(LoginForm.USER_ID, userId);
        form.putSingle(LoginForm.PASSWORD, password);
        form.putSingle(LoginForm.OTML_SECURE_ENCLAVE_PARAMS, LoginForm.OTML_SECURE_ENCLAVE_TOKEN);
        form.putSingle(LoginForm.USE_LOGIN_VIA_FINGERPRINT, "");
        form.putSingle(
                LoginForm.ACTIVATE_FINGERPRINT_MESSAGE, LoginForm.ACTIVATE_FINGERPRINT_LOGIN_VALUE);
        form.putSingle(LoginForm.LANGUAGE, LoginForm.LANGUAGE_EN_US);
        form.putSingle(LoginForm.SUPPORT_FACE_ID, LoginForm.SUPPORT_FACE_ID_FALSE);
        form.putSingle(LoginForm.OPTPARAM, "");
        form.putSingle(LoginForm.SECURITY_CHECK_AVAILABLE, "");
        return form;
    }

    public OtmlResponse getAccountsFromSettings() {
        OtmlResponse response =
                getRequestWithHeaders(Urls.SETTINGS).post(OtmlResponse.class, settingsForm());
        return response;
    }

    private Form settingsForm() {
        Form form = new Form();
        form.putSingle(SettingsForm.SETTINGS_TARGET, SettingsForm.SETTINGS_TARGET_VALUE);
        return form;
    }

    public OtmlResponse getAccountInformationFromAccountMovement(TransactionalAccount account) {
        URL url = getUrlForAccountType(account);

        OtmlResponse response =
                getRequestWithHeaders(url)
                        .post(
                                OtmlResponse.class,
                                getFormForGenericAccount(account.getApiIdentifier()));

        return response;
    }

    public String getMD5OfUpdatePage() {
        String response = getRequestWithHeaders(Urls.UPDATE_PAGE).post(String.class, null);
        return StringUtils.hashAsStringMD5(response).toUpperCase();
    }

    public OtmlResponse getTransactionsForDatePeriod(
            TransactionalAccount account, Date fromDate, Date toDate) {
        URL url = getUrlForAccountType(account);
        OtmlResponse response =
                getRequestWithHeaders(url)
                        .post(
                                OtmlResponse.class,
                                getFormForDatePagination(
                                        account.getApiIdentifier(), fromDate, toDate));
        return response;
    }

    private URL getUrlForAccountType(TransactionalAccount account) {
        URL url = Urls.MOVEMENTS;
        if (account.getType().equals(AccountTypes.SAVINGS)) {
            url = Urls.SAVINGS_MOVEMENTS;
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
                .append(DeviceProfileConfiguration.IOS_STABLE.getMake())
                .append("_")
                .append(DeviceProfileConfiguration.IOS_STABLE.getModelNumber())
                .append("_")
                .append(DeviceProfileConfiguration.IOS_STABLE.getOs())
                .append("_")
                .append(DeviceProfileConfiguration.IOS_STABLE.getOsVersion())
                .toString();
    }

    private String getUserAgent() {
        UserAgentEntity useragent = DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity();
        useragent.setExtensions(Device.USERAGENT_EXTENSION);
        return useragent.toString();
    }

    private RequestBuilder getRequestWithHeaders(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(Header.X_OTML_CLUSTER, getResolution())
                .header(Header.X_DEVICE, getDeviceId())
                .header(Header.USER_AGENT, getUserAgent())
                .header(Header.X_OTML_MANIFEST, sessionStorage.getXOtmlManifest())
                .header(Header.X_OTML_PLATFORM, Application.PLATFORM)
                .header(Header.X_OTMLID, Application.OTMLID)
                .header(Header.X_APPID, Application.PLATFORM_VERSION);
    }

    private Form getFormForGenericAccount(String bankIdentifier) {
        Form form = new Form();
        form.putSingle(
                MovementsSearchForm.SEARCH_TYPE,
                MovementsSearchForm.SEARCH_TYPE_VALUE_LAST_30_DAYS);
        form.putSingle(MovementsSearchForm.ACCOUNT_ID, bankIdentifier);
        form.putSingle(MovementsSearchForm.START_YEAR, "");
        form.putSingle(MovementsSearchForm.START_MONTH, "");
        form.putSingle(MovementsSearchForm.START_DAY, "");
        form.putSingle(MovementsSearchForm.END_YEAR, "");
        form.putSingle(MovementsSearchForm.END_MONTH, "");
        form.putSingle(MovementsSearchForm.END_DAY, "");
        form.putSingle(MovementsSearchForm.TYPE, MovementsSearchForm.TYPE_ALL);
        form.putSingle(GenericForm.SOURCE, GenericForm.SOURCE_OTML);
        form.putSingle(MovementsSearchForm.AMOUNT_TO_POSITIVITY, "+");
        form.putSingle(MovementsSearchForm.AMOUNT_FROM_POSITIVITY, "+");
        form.putSingle(MovementsSearchForm.OTML_BIND_SIDE_CONTENT, "");
        form.putSingle(MovementsSearchForm.FINISH, MovementsSearchForm.FINISH_VALUE);
        form.putSingle(
                MovementsSearchForm.OTML_FIRST_SEARCH,
                MovementsSearchForm.OTML_FIRST_SEARCH_VALUE_FALSE);
        form.putSingle(MovementsSearchForm.AMOUNT_FROM, "");
        form.putSingle(MovementsSearchForm.AMOUNT_TO, "");
        form.putSingle(MovementsSearchForm.TEXT, "");
        return form;
    }

    private Form getFormForDatePagination(String bankIdentifier, Date fromDate, Date toDate) {
        LocalDate localfromDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localtoDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Form form = new Form();
        form.putSingle(
                MovementsSearchForm.SEARCH_TYPE, MovementsSearchForm.SEARCH_TYPE_VALUE_CUSTOM);
        form.putSingle(MovementsSearchForm.ACCOUNT_ID, bankIdentifier);
        form.putSingle(MovementsSearchForm.START_YEAR, Integer.toString(localfromDate.getYear()));
        form.putSingle(
                MovementsSearchForm.START_MONTH, Integer.toString(localfromDate.getMonthValue()));
        form.putSingle(
                MovementsSearchForm.START_DAY, Integer.toString(localfromDate.getDayOfMonth()));
        form.putSingle(MovementsSearchForm.END_YEAR, Integer.toString(localtoDate.getYear()));
        form.putSingle(
                MovementsSearchForm.END_MONTH, Integer.toString(localtoDate.getMonthValue()));
        form.putSingle(MovementsSearchForm.END_DAY, Integer.toString(localtoDate.getDayOfMonth()));
        form.putSingle(MovementsSearchForm.TYPE, MovementsSearchForm.TYPE_ALL);
        form.putSingle(GenericForm.SOURCE, GenericForm.SOURCE_OTML);
        form.putSingle(MovementsSearchForm.AMOUNT_TO, "+");
        form.putSingle(MovementsSearchForm.AMOUNT_FROM_POSITIVITY, "+");
        form.putSingle(MovementsSearchForm.OTML_BIND_SIDE_CONTENT, "");
        form.putSingle(MovementsSearchForm.FINISH, MovementsSearchForm.FINISH_VALUE);
        form.putSingle(
                MovementsSearchForm.OTML_FIRST_SEARCH,
                MovementsSearchForm.OTML_FIRST_SEARCH_VALUE_FALSE);
        form.putSingle(MovementsSearchForm.AMOUNT_FROM, "");
        form.putSingle(MovementsSearchForm.AMOUNT_TO, "");
        form.putSingle(MovementsSearchForm.TEXT, "");
        return form;
    }
}
