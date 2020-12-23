package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet;

import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.http.form.Form;

public class NordnetBaseConstants {

    public static class Urls {

        public static final String BASE = "https://www.nordnet.se";
        public static final String AUTH_BASE = "https://classic.nordnet.se";
        public static final String INIT_LOGIN = BASE + "/api/2/login";
        public static final String BASIC_LOGIN = BASE + "/api/2/authentication/basic/login";
        public static final String INIT_AUTHORIZE = BASE + "/oauth2/authorize";
        public static final String OAUTH2_AUTHORIZE = AUTH_BASE + "/oauth2/authorize";
        public static final String ANONYMOUS_LOGIN = AUTH_BASE + "/api/2/login/anonymous";
        public static final String BANKID_START =
                AUTH_BASE + "/api/2/authentication/eid/se/bankid/start";
        public static final String BANKID_POLL =
                AUTH_BASE + "/api/2/authentication/eid/se/bankid/poll";
        public static final String OAUTH2_TOKEN = BASE + "/oauth2/token";
        public static final String CUSTOMER_INFO = BASE + "/api/2/customers/contact_info";
        public static final String ACCOUNTS = BASE + "/api/2/accounts";
        public static final String ACCOUNT_INFO = BASE + "/api/2/accounts/{account-id}/info";
        public static final String POSITIONS = BASE + "/api/2/accounts/{positions-id}/positions";
    }

    public static class InitLogin {
        public static final String AUTHENTICATED = "authenticated";
    }

    public static class BankIdStatus {
        public static final String OUTSTANDING_TRANSACTION = "outstanding_transaction";
        public static final String USER_SIGN = "user_sign";
        public static final String COMPLETE = "complete";
        public static final String NO_CLIENT = "no_client";
        public static final String ALREADY_IN_PROGRESS = "already_in_progress";
        public static final String USER_CANCEL = "user_cancel";
    }

    public static class StorageKeys {

        public static final String CLIENT_SECRET = "clientSecret";
        public static final String OAUTH2_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String ACCOUNTS = "accounts";
        public static final String SESSION_KEY = "session_key";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SSN = "national_registration_number";
        public static final String USERNAME = "username";
        protected static final byte[] PASSWORD = "password".getBytes();
        public static final String SERVICE = "service";
        public static final String COUNTRY = "country";
        public static final String SESSION_LANGUAGE = "session_lang";
    }

    public static class FormValues {
        public static final String GRANT_TYPE = "authorization_code";
        public static final String CLIENT_SECRET = "6C2B9862-7FEE-CBACE053-3757570ADDEF";
        public static final String ANONYMOUS = "<<anonymous>>";
        public static final String COUNTRY_SE = "SE";
        public static final String LANG_EN = "en";
        public static final Form ANONYMOUS_LOGIN =
                Form.builder()
                        .put(FormKeys.USERNAME, FormValues.ANONYMOUS)
                        .put(new String(FormKeys.PASSWORD), FormValues.ANONYMOUS)
                        .put(FormKeys.SERVICE, QueryValues.CLIENT_ID)
                        .put(FormKeys.COUNTRY, FormValues.COUNTRY_SE)
                        .put(FormKeys.SESSION_LANGUAGE, FormValues.LANG_EN)
                        .build();
    }

    public static final class HeaderKeys {
        public static final String NTAG = "ntag";
        public static final String LOCATION = "Location";
        public static final String APPLICATION_XML_Q = "application/xml;q=0.9";
        public static final String GENERIC_MEDIA_TYPE = "*/*;q=0.8";
        public static final String REFERER = "Referer";
        public static final String BEARER = "Bearer ";
        public static final String BASIC = "Basic ";
    }

    public static final class HeaderValues {
        public static final String NO_NTAG_RECEIVED_YET = "NO_NTAG_RECEIVED_YET";
        public static final String USER_AGENT =
                "Mozilla/5.0 (iPhone; CPU iPhone OS 13_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148";
        public static final String NORDNET_AGENT = "Nordnet/7 CFNetwork/1121.2.2 Darwin/19.3.0";
        public static final String REACT_NATIVE_AGENT =
                "Nordnet React Native App/ios-13.3.1/10.10.0";
        public static final String REFERER =
                "https://classic.nordnet.se/mux/login/startSE.html?clearEndpoint=0&intent=mobile_ios_2&state=signin";
    }

    public static class QueryKeys {
        public static final String AUTH_TYPE = "authType";
        public static final String INCLUDE_INSTRUMENT_LOAN = "include_instrument_loans";
    }

    public static class QueryValues {
        public static final String AUTH_TYPE = "signin";
        public static final String CLIENT_ID = "MOBILE_IOS_2";
        public static final String RESPONSE_TYPE = "code";
        public static final String REDIRECT_URI = "nordnet-react://oauth2/authorize-callback";
        public static final String B = "1";
        public static final String TRUE = "true";
    }

    public static class Patterns {
        public static final Pattern CODE = Pattern.compile("\\?code=([a-zA-Z\\d]*)$");
    }

    public static class Errors {
        public static final String INVALID_SESSION = "NEXT_INVALID_SESSION";
        public static final String ERROR = "error";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "account-id";
        public static final String POSITIONS_ID = "positions-id";
        public static final String CLIENT_ID = "client-id";
    }

    public static class NordnetAccountTypes {
        public static final String AKTIE_FONDKONTO_AF = "AF";
        public static final String AKTIE_FONDKONTO_EKF = "EKF";
        public static final String INVESTERINGSSPARKONTO = "ISK";
        public static final String KAPITALFORSAKRING = "KF";
        public static final String SPARKONTO = "S";
        public static final String IPS = "IPS";
        public static final String FKF = "FKF";
        public static final String PRIVATE_PENSION_PP = "PP";
        public static final String TJANSTEPENSION_TJP = "TJP";
        public static final String DEPOT = "DEP";
        public static final String TJANSTEPENSION_TJF = "TJF";
        public static final String TJANSTEPENSION_BTP = "BTP";
        public static final String AVTALS_PENSION = "KAP";
        public static final String KAPITAL_PENSION = "KP";
        public static final String AOT = "AOT";
        public static final String TT = "TT";
        public static final String OST = "OST";
    }

    public static TypeMapper<AccountTypes> getAccountTypeMapper() {

        return TypeMapper.<AccountTypes>builder()
                .put(
                        AccountTypes.INVESTMENT,
                        NordnetAccountTypes.AKTIE_FONDKONTO_AF,
                        NordnetAccountTypes.AKTIE_FONDKONTO_EKF,
                        NordnetAccountTypes.FKF,
                        NordnetAccountTypes.INVESTERINGSSPARKONTO,
                        NordnetAccountTypes.KAPITALFORSAKRING,
                        NordnetAccountTypes.AOT,
                        NordnetAccountTypes.TT,
                        NordnetAccountTypes.OST)
                .put(AccountTypes.SAVINGS, NordnetAccountTypes.SPARKONTO)
                .put(
                        AccountTypes.PENSION,
                        NordnetAccountTypes.PRIVATE_PENSION_PP,
                        NordnetAccountTypes.IPS,
                        NordnetAccountTypes.AVTALS_PENSION,
                        NordnetAccountTypes.KAPITAL_PENSION,
                        NordnetAccountTypes.TJANSTEPENSION_TJP,
                        NordnetAccountTypes.TJANSTEPENSION_TJF,
                        NordnetAccountTypes.TJANSTEPENSION_BTP)
                .build();
    }

    public static class NordnetAccountCodes {
        public static final String INVESTERINGSSPARKONTO = "ISK";
        public static final String PRIVATE_PENSION_PT = "PT";
        public static final String KAPITALFORSAKRING = "KF";
        public static final String DEPOT = "DEP";
        public static final String TJANSTEPENSION_TJF = "TJF";
        public static final String TJANSTEPENSION_TJFF = "TJFF";
        public static final String TJANSTEPENSION_BTP_TRYGG = "BTP_TRYGG";
        public static final String TJANSTEPENSION_BTP_VALBAR = "BTP_VALBAR";
        public static final String IPS = "IPS";
        public static final String TJANSTEPENSION_BTP1 = "BTP1";
        public static final String AVTALS_PENSION = "KAP_KL";
        public static final String KAPITAL_PENSION = "KP";
        public static final String PENSION_KFEX = "KFEX";
    }

    public static TypeMapper<PortfolioModule.PortfolioType> getPortfolioTypeMapper() {
        return TypeMapper.<PortfolioModule.PortfolioType>builder()
                .put(PortfolioModule.PortfolioType.DEPOT, NordnetAccountCodes.DEPOT)
                .put(PortfolioModule.PortfolioType.ISK, NordnetAccountCodes.INVESTERINGSSPARKONTO)
                .put(PortfolioModule.PortfolioType.KF, NordnetAccountCodes.KAPITALFORSAKRING)
                .put(
                        PortfolioModule.PortfolioType.PENSION,
                        NordnetAccountTypes.IPS,
                        NordnetAccountCodes.AVTALS_PENSION,
                        NordnetAccountCodes.KAPITAL_PENSION,
                        NordnetAccountCodes.PENSION_KFEX,
                        NordnetAccountCodes.PRIVATE_PENSION_PT,
                        NordnetAccountCodes.TJANSTEPENSION_TJFF,
                        NordnetAccountCodes.TJANSTEPENSION_TJF,
                        NordnetAccountCodes.TJANSTEPENSION_BTP1,
                        NordnetAccountCodes.TJANSTEPENSION_BTP_TRYGG,
                        NordnetAccountCodes.TJANSTEPENSION_BTP_VALBAR)
                .build();
    }

    public static class NordnetRetryFilter {
        public static final int NUM_TIMEOUT_RETRIES = 5;
        public static final int RETRY_SLEEP_MILLISECONDS = 5000;
    }
}
