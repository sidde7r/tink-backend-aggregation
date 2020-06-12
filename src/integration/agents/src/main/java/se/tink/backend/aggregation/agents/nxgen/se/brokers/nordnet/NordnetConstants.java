package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet;

import java.util.Arrays;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;

public class NordnetConstants {

    public enum BankIdResponseStatus {
        NO_CLIENT("NO_CLIENT"),
        USER_SIGN("USER_SIGN"),
        COMPLETE("COMPLETE"),
        CANCELLED("USER_CANCEL"),
        TIMEOUT("EXPIRED_TRANSACTION"),
        ALREADY_IN_PROGRESS("OUTSTANDING_TRANSACTION"),
        UNKNOWN("");

        private String statusCode;

        BankIdResponseStatus(String statusCode) {
            this.statusCode = statusCode;
        }

        public static BankIdResponseStatus fromStatusCode(String statusCode) {
            return Arrays.stream(BankIdResponseStatus.values())
                    .filter(status -> status.getStatusCode().equalsIgnoreCase(statusCode))
                    .findFirst()
                    .orElse(BankIdResponseStatus.UNKNOWN);
        }

        public String getStatusCode() {
            return statusCode;
        }
    }

    public static class FormKeys {
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String SERVICE = "service";
        public static final String COUNTRY = "country";
        public static final String SESSION_LANGUAGE = "session_lang";
        public static final String GRANT_TYPE = "grant_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String CODE = "code";
        public static final String SAML_REQUEST = "SAMLResponse";
        public static final String TARGET = "TARGET";
    }

    public static class FormValues {
        public static final String ANONYMOUS = "<<anonymous>>";
        public static final String COUNTRY_SE = "SE";
        public static final String LANG_EN = "en";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REDIRECT_URI = "https://www.nordnet.se/now/mobile/token.html";
    }

    public static class Urls {

        public static final String BASE_URL = "https://classic.nordnet.se";
        public static final String BANKID_ORDER_SUFFIX = "order";
        public static final String BANKID_COMPLETE_SUFFIX = "complete";
        public static final String BASE_OAUTH2_AUTHORIZE = BASE_URL + "/oauth2/authorize";
        public static final String BASE_API_2 = BASE_URL + "/api/2";
        public static final String BASE_API_2_AUTHENTICATION = BASE_API_2 + "/authentication";
        public static final String AUTHENTICATION_BASIC_LOGIN_URL =
                BASE_API_2_AUTHENTICATION + "/basic/login";
        public static final String AUTHENTICATION_SAML_ARTIFACT =
                BASE_API_2_AUTHENTICATION + "/eid/saml/artifact";
        public static final String INIT_LOGIN_SESSION_URL_PASSWORD = BASE_API_2 + "/login";
        public static final String INIT_LOGIN_SESSION_URL_BANKID =
                INIT_LOGIN_SESSION_URL_PASSWORD + "/anonymous";
        public static final String LOGIN_BANKID_PAGE_URL =
                BASE_API_2_AUTHENTICATION + "/eid/saml/request";
        public static final String FETCH_TOKEN_URL = BASE_URL + "/oauth2/token";
        public static final String ENVIRONMENT_URL = BASE_API_2 + "/system/environment";
        public static final String GET_ACCOUNTS_URL = BASE_API_2 + "/accounts";
        public static final String GET_ACCOUNTS_INFO_URL_2 =
                BASE_API_2 + "/accounts/{account-id}/info";
        public static final String GET_POSITIONS_URL_2 =
                BASE_API_2 + "/accounts/{positions-id}/positions";
        public static final String GET_CUSTOMER_INFO_URL = BASE_API_2 + "/customers/contact_info";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "account-id";
        public static final String POSITIONS_ID = "positions-id";
        public static final String CLIENT_ID = "client-id";
    }

    public static class QueryKeys {
        public static final String AUTH_TYPE = "authType";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String EID_METHOD = "eid_method";
        public static final String INCLUDE_INSTRUMENT_LOAN = "include_instrument_loans";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String REDIRECT_URI_LOGIN = "redirect_uri";
    }

    public static class QueryParamValues {
        public static final String CLIENT_ID = "MOBILE_IOS_2";
        public static final String CLIENT_SECRET = "6C2B9862-7FEE-CBACE053-3757570ADDEF";
        public static final String SIGN_IN = "signin";
        public static final String RESPONSE_TYPE = "code";
        public static final String REDIRECT_URI = "https://www.nordnet.se/now/mobile/token.html";
        public static final String REDIRECT_URI_LOGIN = "nordnet-react://oauth2/authorize-callback";
        public static final String EID_METHOD = "sbidAnother";
        public static final String INCLUDE_INSTRUMENT_LOAN = "true";
    }

    public static final class HeaderKeys {

        public static final String NTAG = "ntag";
        public static final String REFERRER = "Referer";
        public static final String LOCATION = "location";
        public static final String APPLICATION_XML_Q = "application/xml;q=0.9";
        public static final String GENERIC_MEDIA_TYPE = "*/*;q=0.8";
    }

    public static class Patterns {
        public static final Pattern FIND_CODE_FROM_URI = Pattern.compile("\\?code=([a-zA-Z\\d]*)$");
        public static final Pattern FIND_SAMLART_FROM_URI = Pattern.compile("SAMLart=([^&]*)");
        public static final Pattern FIND_BANKID_URL =
                Pattern.compile(
                        "https://nneid\\.nordnet\\.se/std/method/nordnet\\.se/[a-zA-Z\\d]*/");
    }

    public static class StorageKeys {
        public static final String ORDER_REF = "orderRef";
        public static final String AUTO_START_TOKEN = "autoStartToken";
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String ACCOUNTS = "accounts";
        public static final String SESSION_KEY = "session_key";
        public static final String NTAG = "ntag";
    }

    public static class Errors {
        public static final String INVALID_SESSION = "NEXT_INVALID_SESSION";
        public static final String INVALID_LOGIN_PARAMETER = "NEXT_LOGIN_INVALID_LOGIN_PARAMETER";
        public static final String ALREADY_IN_SESSION = "ALREADY_IN_PROGRESS";
    }

    public static class NordnetAccountTypes {
        public static final String AKTIE_FONDKONTO = "AF";
        public static final String INVESTERINGSSPARKONTO = "ISK";
        public static final String KAPITALFORSAKRING = "KF";
        public static final String SPARKONTO = "S";
        public static final String IPS = "IPS";
        public static final String PRIVATE_PENSION = "PP";
        public static final String TJANSTEPENSION = "TJP";
        public static final String DEPOT = "dep";
        public static final String TJANSTEPENSION_TJF = "tjf";
        public static final String TJANSTEPENSION_BTP1 = "btp1";
        public static final String TJANSTEPENSION_BTP = "btp";
    }

    public static TypeMapper<AccountTypes> getAccountTypeMapper() {

        return TypeMapper.<AccountTypes>builder()
                .put(
                        AccountTypes.INVESTMENT,
                        NordnetAccountTypes.AKTIE_FONDKONTO,
                        NordnetAccountTypes.INVESTERINGSSPARKONTO,
                        NordnetAccountTypes.KAPITALFORSAKRING)
                .put(AccountTypes.SAVINGS, NordnetAccountTypes.SPARKONTO)
                .put(
                        AccountTypes.PENSION,
                        NordnetAccountTypes.PRIVATE_PENSION,
                        NordnetAccountTypes.IPS,
                        NordnetAccountTypes.TJANSTEPENSION,
                        NordnetAccountTypes.TJANSTEPENSION_TJF,
                        NordnetAccountTypes.TJANSTEPENSION_BTP)
                .build();
    }

    public static TypeMapper<PortfolioModule.PortfolioType> getPortfolioTypeMapper() {
        return TypeMapper.<PortfolioModule.PortfolioType>builder()
                .put(PortfolioModule.PortfolioType.DEPOT, NordnetAccountTypes.DEPOT)
                .put(PortfolioModule.PortfolioType.ISK, NordnetAccountTypes.INVESTERINGSSPARKONTO)
                .put(PortfolioModule.PortfolioType.KF, NordnetAccountTypes.KAPITALFORSAKRING)
                .put(
                        PortfolioModule.PortfolioType.PENSION,
                        NordnetAccountTypes.IPS,
                        NordnetAccountTypes.TJANSTEPENSION,
                        NordnetAccountTypes.TJANSTEPENSION_TJF,
                        NordnetAccountTypes.PRIVATE_PENSION,
                        NordnetAccountTypes.TJANSTEPENSION_BTP1,
                        NordnetAccountTypes.TJANSTEPENSION_BTP)
                .build();
    }
}
