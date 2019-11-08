package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import com.google.common.base.Charsets;
import java.nio.charset.Charset;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class JyskeConstants {

    public static final Charset CHARSET = Charsets.UTF_8;

    public static final class Url {
        private static final String BANKDATA_BASE_URL = "https://mobil.bankdata.dk/mobilbank";
        private static final String SERVICE_BASE_URL =
                "https://mobile-services.jyskebank.dk/mobilebank.services/rest";

        public static final URL NEMID_INIT = toBankDataUrl("/nemid/init");

        public static final URL NEMID_GET_CHALLANGE = toBankDataUrl("/nemid/get_challange");

        public static final URL NEMID_ENROLL = toBankDataUrl("/nemid/inroll");
        public static final URL NEMID_LOGIN = toBankDataUrl("/nemid/login_with_installid_prop");
        public static final URL GET_ACCOUNTS_WITH_EXTERNALS = toBankDataUrl("/accounts");

        public static final URL GET_TRANSACTIONS_WITH_EXTERNALS =
                toBankDataUrl("/pfm/transactions");
        public static final URL GET_FUTURE_TRANSACTIONS = toBankDataUrl("/pfm/transactions/future");
        public static final URL GET_INVESTMENT_GROUPS = toBankDataUrl("/investment/groups");
        public static final URL LOGOUT = toBankDataUrl("/invalidate");
        public static final URL TRANSPORT_KEY = toMobileServiceUrl("/V1-0/transportkey");
        public static final URL MOBILE_SERVICE_LOGIN = toMobileServiceUrl("/V1-0/login");
        public static final URL GET_CARDS = toMobileServiceUrl("/V1-0/cardapp/cards");

        private static URL toBankDataUrl(String endpoint) {
            return new URL(BANKDATA_BASE_URL + endpoint);
        }

        private static URL toMobileServiceUrl(String endpoint) {
            return new URL(SERVICE_BASE_URL + endpoint);
        }
    }

    public static final class Header {
        public static final String APP_ID_KEY = "x-app-id";
        public static final String APP_ID_VALUE = "ios_phone_jyskemobilbank";
        public static final String APPID_KEY = "x-appid";
        public static final String APPID_VALUE = APP_ID_VALUE;
        public static final String VERSION_KEY = "x-version";
        public static final String VERSION_VALUE = "3.20.5";
        public static final String BANKNO_KEY = "x-bankNo";
        public static final String BANKNO_VALUE = "51";
        public static final String OS_KEY = "x-os";
        public static final String OS_VALUE = "ios";

        public static final String BUILDNO_KEY = "x-buildNo";
        public static final String BUILDNO_VALUE = "1364";

        public static final String PERSONALID_KEY = "x-personalId";
    }

    public static final class Crypto {
        public static final String RSA_LABEL = "jbprodver001";
        public static final String CERT_TYPE = "X.509";
    }

    public static final class Storage {
        public static final String INSTALL_ID = "installId";
    }

    public static final class ErrorCode {
        public static final int INVALID_CREDENTIAL = 112;
        public static final int NOT_SIGNED_UP_FOR_MOBILE_BANK = 109;
        public static final int INROLL_BAD_REQUEST = 1;
    }

    public static final class ErrorMessages {
        public static final String BANK_UNAVAILABLE_DURING_MIDNIGHT =
                "mobilbanken er lukket hverdage og ";
    }

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.SAVINGS, "Jyske Munnypot", "Opsparing")
                    .put(
                            AccountTypes.CHECKING,
                            "Budget",
                            "Totalkonto",
                            "Totalkonto Ung",
                            "Lønkonto",
                            "Budgetkonto",
                            "Budgetkonto Ung",
                            "Grundkonto",
                            "Forbrug")
                    .build();

    public static final class Log {
        public static final LogTag CREDITCARD_LOGGING = LogTag.from("#dk_jyske_creditcard");
        public static final LogTag INVESTMENT_LOGGING = LogTag.from("#dk_jyske_investment");
    }

    public static final class Fetcher {

        public static final class CreditCard {
            public static final String DANKORT = "DANKORT";
            public static final String DEBIT = "DEBIT";
        }

        public static final class Investment {
            public static final String PENSION_TYPE = "pension";
            public static final String CHILD_SAVING_TYPE = "childsaving";
            public static final String CURRENCY = "DKK";
        }
    }

    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
