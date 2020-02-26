package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public class LansforsakringarConstants {

    public static final int MAX_BANKID_LOGIN_ATTEMPTS = 50;

    public static class Urls {
        public static final String BASE = "https://mobil.lansforsakringar.se/";

        public static final URL FETCH_TRANSACTIONS = new URL(BASE + ApiService.FETCH_ACCOUNTS);
        public static final URL INIT_BANKID = new URL(BASE + ApiService.INIT_BANKID);
        public static final URL LOGIN_BANKID = new URL(BASE + ApiService.LOGIN_BANKID);
        public static final URL FETCH_TRANSACTIONs = new URL(BASE + ApiService.FETCH_TRANSACTIONS);
        public static final URL FETCH_UPCOMING = new URL(BASE + ApiService.FETCH_UPCOMING);
        public static final URL FETCH_CARDS = new URL(BASE + ApiService.FETCH_CARDS);
        public static final URL FETCH_PENSION_WITH_LIFE_INSURANCE =
                new URL(BASE + ApiService.PENSION_WITH_LIFE_INSURANCE);
        public static final URL FETCH_PENSION_OVERVIEW =
                new URL(BASE + ApiService.PENSION_OVERVIEW);
    }

    public static class ApiService {
        public static final String FETCH_TRANSACTIONS = "es/deposit/gettransactions/3.0";
        public static final String FETCH_ACCOUNTS = "appoutlet/startpage/getengagements/4.0";
        public static final String INIT_BANKID = "appoutlet/security/user/bankid/authenticate";
        public static final String LOGIN_BANKID = "appoutlet/security/user/bankid/login/3.0";
        public static final String FETCH_UPCOMING = "appoutlet/account/upcoming/7.0";
        public static final String FETCH_CARDS = "appoutlet/card/list/3.0";
        public static final String PENSION_WITH_LIFE_INSURANCE =
                "es/lifeinsurance/getengagements/1.0";
        public static final String PENSION_OVERVIEW = "appoutlet/pension/overview/withtotal/2.0";
    }

    public static class StorageKeys {
        public static final String SSN = "ssn";
        public static final String NAME = "name";
        public static final String TICKET = "ticket";
        public static final String ENTERPRISE_SERVICE_PRIMARY_SESSION =
                "enterpriseServicesPrimarySession";
        public static final String CUSTOMER_NAME = "name";
    }

    public static class HeaderKeys {
        public static final String DEVICE_ID = "DeviceId";
        public static final String DEVICE_INFO = "deviceInfo";
        public static final String USER_AGENT = "User-Agent";
        public static final String ERROR_CODE = "Error-Code";
        public static final String USER_SESSION = "USERSESSION";
        public static final String UTOKEN = "Utoken";
    }

    public static class Accounts {
        public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CHECKING, "CHECKING", "DEBIT")
                        .put(AccountTypes.SAVINGS, "SAVINGS")
                        .put(AccountTypes.PENSION, "PENSION")
                        .build();
        public static final String CURRENCY = "SEK";
    }

    public static final class Fetcher {
        public static final int START_PAGE = 0;
        public static final String CUSTOMER_PROFILE_TYPE = "CUSTOMER";
        public static final String BOOKED_TRANSACTION_STATUS = "BOOKED";
        public static final String PENDING_TRANSACTION_STATUS = "PENDING";
        public static final String PENSION_ACCOUNT_TYPE = "PENSION";
    }

    public enum UserMessage implements LocalizableEnum {
        MUST_ACCEPT_TERMS(
                new LocalizableKey(
                        "The first time you use your BankId, you need to accept the terms and conditions. Please login to the Länsförsäkringar with your moible BankId to do this"));
        private final LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return this.userMessage;
        }
    }
}
