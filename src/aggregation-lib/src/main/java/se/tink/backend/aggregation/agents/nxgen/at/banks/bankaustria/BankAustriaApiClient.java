package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria;

import com.sun.jersey.api.representation.Form;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.entities.OtmlResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.strings.StringUtils;

import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class BankAustriaApiClient {
    private final TinkHttpClient client;
    private final BankAustriaSessionStorage sessionStorage;

    public BankAustriaApiClient(TinkHttpClient client, BankAustriaSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }


    public OtmlResponse login(String userId, String password)  {
        OtmlResponse response = getRequestWithHeaders(BankAustriaConstants.Urls.LOGIN)
                .post(OtmlResponse.class, loginForm(userId, password));
        return response;
    }

    public void logout() {
        getRequestWithHeaders(BankAustriaConstants.Urls.LOGOUT).post();
    }


    private Form loginForm(String userId, String password) {
        Form form = new Form();
        form.putSingle("userId", userId);
        form.putSingle("password", password);
        form.putSingle("otml_secure_enclave_params", "token");
        form.putSingle("useLoginViaFingerprint", "");
        form.putSingle("activate_fingerprint_message", "Activate%20fingerprint%20login");
        form.putSingle("language", "en_US");
        form.putSingle("supportFaceId", "false");
        form.putSingle("optparam", "");
        form.putSingle("securityCheckAvailable", "");
        return form;
    }


    public OtmlResponse getAccountsFromSettings() {
        OtmlResponse response = getRequestWithHeaders(BankAustriaConstants.Urls.SETTINGS)
                .post(OtmlResponse.class, settingsForm());
        return response;
    }

    private Form settingsForm() {
        Form form = new Form();
        form.putSingle("_target0", "xxx");
        return form;
    }

    public OtmlResponse getAccountInformationFromAccountMovement(String bankIdentifier) {
        OtmlResponse response = getRequestWithHeaders(BankAustriaConstants.Urls.MOVEMENTS)
                .post(OtmlResponse.class, getFormForGenericAccount(bankIdentifier));
        return response;
    }

    public String getMD5OfUpdatePage() {
        String response = getRequestWithHeaders(BankAustriaConstants.Urls.UPDATE_PAGE)
                .post(String.class, null);
        return StringUtils.hashAsStringMD5(response).toUpperCase();
    }

    public OtmlResponse getTransactionsForDatePeriod(TransactionalAccount account, Date fromDate, Date toDate) {
        OtmlResponse response = getRequestWithHeaders(BankAustriaConstants.Urls.MOVEMENTS)
                .post(OtmlResponse.class, getFormForDatePagination(account.getBankIdentifier(), fromDate, toDate));
        return response;
    }

    private RequestBuilder getRequestWithHeaders(URL url) {

        return client.request(url)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header("X-OTML-CLUSTER", BankAustriaConstants.Device.IPHONE7_RESOLUTION)
                .header("X-DEVICE", BankAustriaConstants.Device.IPHONE7_DEVICEID)
                .header("User-Agent", BankAustriaConstants.Device.IPHONE7_USERAGENT)
                .header("X-OTML-MANIFEST", sessionStorage.getXOtmlManifest())
                .header("X-OTML-PLATFORM", BankAustriaConstants.Application.PLATFORM)
                .header("X-OTMLID", BankAustriaConstants.Application.OTMLID)
                .header("X-APPID", BankAustriaConstants.Application.PLATFORM_VERSION);
    }


    private Form getFormForGenericAccount(String bankIdentifier) {
        Form form = new Form();
        form.putSingle("searchType", "30");
        form.putSingle("account.id", bankIdentifier);
        form.putSingle("start.year", "");
        form.putSingle("start.month", "");
        form.putSingle("start.day", "");
        form.putSingle("end.year", "");
        form.putSingle("end.month", "");
        form.putSingle("end.day", "");
        form.putSingle("type", "all");
        form.putSingle("source", "otml");
        form.putSingle("amountToPositivity", "+");
        form.putSingle("amountFromPositivity", "+");
        form.putSingle(".otmlBind->side_content", "");
        form.putSingle("_finish", "xxx");
        form.putSingle("otmlFirstSearch", "false");
        form.putSingle("amountFrom", "");
        form.putSingle("amountTo", "");
        form.putSingle("text", "");
        return form;
    }


        private Form getFormForDatePagination(String bankIdentifier, Date fromDate, Date toDate) {
        LocalDate localfromDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localtoDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Form form = new Form();
        form.putSingle("searchType", "custom");
        form.putSingle("account.id", bankIdentifier);
        form.putSingle("start.year", Integer.toString(localfromDate.getYear()));
        form.putSingle("start.month", Integer.toString(localfromDate.getMonthValue()));
        form.putSingle("start.day", Integer.toString(localfromDate.getDayOfMonth()));
        form.putSingle("end.year", Integer.toString(localtoDate.getYear()));
        form.putSingle("end.month", Integer.toString(localtoDate.getMonthValue()));
        form.putSingle("end.day", Integer.toString(localtoDate.getDayOfMonth()));
        form.putSingle("type", "all");
        form.putSingle("source", "otml");
        form.putSingle("amountToPositivity", "+");
        form.putSingle("amountFromPositivity", "+");
        form.putSingle(".otmlBind->side_content", "");
        form.putSingle("_finish", "xxx");
        form.putSingle("otmlFirstSearch", "false");
        form.putSingle("amountFrom", "");
        form.putSingle("amountTo", "");
        form.putSingle("text", "");
        return form;
    }
}
