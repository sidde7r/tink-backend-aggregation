package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki;

import java.util.Arrays;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AgentBaseError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.entities.StatusEntity;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.i18n.LocalizableKey;

public class SpankkiConstants {
    private static final String BASE_URL = "https://mobile.s-pankki.fi";

    public static class Authentication {
        public static final String REQUEST_TOKEN_RANDOM_STRING = "6b6e21bc4e6e4a2c8caf49042220b554";
        public static final String CHALLENGE_RESPONSE_RANDOM_STRING =
                "5f41cbaea5a74b3c886216ebc5bcefd8";

        public static final String USER_DEVICE_NAME = "Tink";
        public static final int KEY_CARD_VALUE_LENGTH = 4;

        public static final String PASSWORD_STATUS_CHANGE = "CHANGE";
        public static final LocalizableKey PASSWORD_CHANGE_MSG =
                new LocalizableKey(
                        "You need to change your password at S-Pankki before you can login.");
    }

    public enum AccountType {
        HOUSE_SAVINGS_ACCOUNT("443", AccountTypes.SAVINGS),
        SAVINGS_ACCOUNT("419", AccountTypes.SAVINGS),
        INVESTMENT_ACCOUNT("427", AccountTypes.SAVINGS),
        SPANKKI_ACCOUNT("418", AccountTypes.CHECKING),
        SPANKKI_FUND("416", AccountTypes.SAVINGS),
        CURRENT_ACCOUNT("415", AccountTypes.CHECKING),
        UNKOWN("", AccountTypes.OTHER);

        AccountType(String sPankkiAccountType, AccountTypes tinkType) {
            this.sPankkiAccountType = sPankkiAccountType;
            this.tinkType = tinkType;
        }

        private final String sPankkiAccountType;
        private final AccountTypes tinkType;

        public static AccountType toAccountType(String sAccountType) {
            for (AccountType accountType : AccountType.values()) {
                if (accountType.sPankkiAccountType.equalsIgnoreCase(sAccountType)) {
                    return accountType;
                }
            }

            return AccountType.UNKOWN;
        }

        public AccountTypes getTinkType() {
            return tinkType;
        }

        public String getSPankkiAccountType() {
            return sPankkiAccountType;
        }
    }

    public static class Request {
        public static final String CLIENT_INFO_PLATFORM_NAME = "iPhone9,3";
        public static final String CLIENT_INFO_APP_VERSION = "1.28.1";
        public static final String CLIENT_INFO_LANG = "sv";
        public static final String CLIENT_INFO_PLATFORM_TYPE = "ios";
        public static final String CLIENT_INFO_APP_NAME = "spankki";
        public static final String CLIENT_INFO_PLATFORM_VERSION = "11.1.1";
        public static final String CLIENT_INFO_DEVICE_MODEL = "iPhone 6s";
    }

    public static class LogTags {
        public static final LogTag LOG_TAG_CREDIT_CARD = LogTag.from("#spankki_creditcard");
        public static final LogTag LOG_TAG_ACCOUNT_TYPE = LogTag.from("#spankki_accounttype");
    }

    public enum ServerResponse {
        OK(0, null),
        LOGIN_FAILED(100, LoginError.INCORRECT_CREDENTIALS),
        PIN_ERROR(101, LoginError.INCORRECT_CREDENTIALS),
        BANK_SYSTEM_OFFLINE(111, BankServiceError.NO_BANK_SERVICE),
        BLOCKED_USER(120, AuthorizationError.ACCOUNT_BLOCKED),
        UNKNOWN_ERROR(-1, null);

        private final int statusCode;
        private final AgentBaseError agentError;

        ServerResponse(int statusCode, AgentBaseError agentError) {

            this.statusCode = statusCode;
            this.agentError = agentError;
        }

        public static ServerResponse getMessage(StatusEntity status) {
            return Arrays.stream(ServerResponse.values())
                    .filter(
                            serverMessage ->
                                    status.getStatusCode() == serverMessage.getStatusCode())
                    .findFirst()
                    .orElse(ServerResponse.UNKNOWN_ERROR);
        }

        public static void throwIfError(StatusEntity status)
                throws AuthenticationException, AuthorizationException {

            ServerResponse serverResponse = ServerResponse.getMessage(status);

            if (serverResponse.agentError != null) {
                throwAgentError(serverResponse.agentError.exception());
            }
            if (serverResponse.statusCode < 0) {
                throw new IllegalStateException(status.getErrorMessage());
            }
        }

        private static void throwAgentError(Throwable e)
                throws AuthenticationException, AuthorizationException {
            if (e instanceof AuthenticationException) {
                throw (AuthenticationException) e;
            } else if (e instanceof AuthorizationException) {
                throw (AuthorizationException) e;
            }
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public enum Url {
        REQUEST_CHALLENGE(new URL(BASE_URL + "/smob/rest/v1/device/authenticate/chreq"), ""),
        RESPOND_TO_CHALLENGE(new URL(BASE_URL + "/smob/rest/v1/device/authenticate/chresp"), ""),
        LOGIN_USERNAME_PASSWORD(
                new URL(BASE_URL + "/smob/rest/v1/customer/login/usrpwd"), "crSessionToken"),
        LOGIN_PIN(new URL(BASE_URL + "/smob/rest/v1/customer/login/pin"), "crSessionToken"),
        LOGIN_DEVICE_TOKEN(
                new URL(BASE_URL + "/smob/rest/v1/customer/login/token"),
                "tokenLogin-request-token"),
        ADD_DEVICE(new URL(BASE_URL + "/smob/rest/v1/customer/devices/add"), "adddeviceReqToken"),
        GET_ACCOUNTS(
                new URL(BASE_URL + "/smob/rest/v1/customer/accounts/get"), "get-accounts-token"),
        GET_TRANSACTIONS(
                new URL(BASE_URL + "/smob/rest/v1/customer/transactions/get"),
                "get-transactions-token"),
        RESERVATIONS(new URL(BASE_URL + "/smob/rest/v1/reservations"), ""),
        CARDS_OVERVIEW(
                new URL(BASE_URL + "/smob/rest/v1/customer/cards/overview"),
                "get-cardsoverview-token"),
        LOAN_OVERVIEW(new URL(BASE_URL + "/smob/rest/v1/loan/loanOverview"), "BaseRequest-token"),
        LOAN_DETAILS(new URL(BASE_URL + "/smob/rest/v1/loan/loanDetails"), "BaseRequest-token"),
        FUNDS_PORTFOLIOS(new URL(BASE_URL + "/smob/rest/v1/customer/funds/portfolios"), ""),
        GET_ALL_FUNDS(new URL(BASE_URL + "/smob/rest/v1/customer/funds/getFunds"), ""),
        LOGOUT(new URL(BASE_URL + "/smob/rest/v1/customer/login/logout"), "");

        private final URL url;
        private final String requestToken;

        Url(URL url, String requestToken) {
            this.url = url;
            this.requestToken = requestToken;
        }

        public String getRequestToken() {
            return this.requestToken;
        }

        public URL getUrl() {
            return this.url;
        }
    }

    public static class Storage {
        public static final String SESSION_ID = "SESSION_ID";
        public static final String DEVICE_ID = "DEVICE_ID";
        public static final String DEVICE_TOKEN = "DEVICE_TOKEN";
        public static final String CUSTOMER_ID = "CUSTOMER_ID";
        public static final String CUSTOMER_ENTITY = "CUSTOMER_ENTITY";
    }

    public static class Loan {
        public static final String STUDENT_LOAN_NAME_FI = "OPINTOLAINA";
        public static final String INTEREST_BINDING_TEXT = " kk:n euriborkorko";
    }

    public static class Investment {
        public static final String AGGREGATE_PORTFOLIO = "AGGREGATE_PORTFOLIO";
        public static String ACCOUNT_ID_PREFIX = "FUND-";

        public enum PortfolioType {
            DEPOT("PORTFOLIO", Portfolio.Type.DEPOT),
            UNKOWN("U|N|K|N|O|W|N", Portfolio.Type.OTHER);

            PortfolioType(String sPankkiPortfolioType, Portfolio.Type tinkType) {
                this.sPankkiPortfolioType = sPankkiPortfolioType;
                this.tinkType = tinkType;
            }

            private final String sPankkiPortfolioType;
            private final Portfolio.Type tinkType;

            public static Portfolio.Type toTinkType(String sPortfolioType) {
                for (PortfolioType portfolioType : PortfolioType.values()) {
                    if (("" + sPortfolioType)
                            .toUpperCase()
                            .contains(portfolioType.sPankkiPortfolioType.toUpperCase())) {
                        return portfolioType.tinkType;
                    }
                }

                return PortfolioType.UNKOWN.tinkType;
            }

            public Portfolio.Type getTinkType() {
                return tinkType;
            }
        }
    }
}
