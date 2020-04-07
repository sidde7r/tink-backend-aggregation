package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SdcNoConstants {

    private static final Pattern BANKCODE_PATTERN = Pattern.compile("\\{bankcode}");

    private SdcNoConstants() {}

    public static class PortalBank {
        private PortalBank() {}

        public static final String BASE_URL = "https://www.portalbank.no/";

        static final String LOGIN_URL = "https://id.portalbank.no/wsl/slogin/Run?n_bank={bankcode}";

        static final Matcher LOGIN_MATCHER = BANKCODE_PATTERN.matcher(PortalBank.LOGIN_URL);
    }

    public static class NettBankPortal {
        private NettBankPortal() {}

        public static final String BASE_URL = "https://www.nettbankportal.no/";
        static final String LOGIN_URL =
                "https://www.nettbankportal.no/{bankcode}/nettbank2/logon/bankidjs/?portletname=bankidloginjs&portletaction=openmobilelogin";

        static final Matcher LOGIN_MATCHER = BANKCODE_PATTERN.matcher(NettBankPortal.LOGIN_URL);
    }

    public static class EikaBankPortal {
        private EikaBankPortal() {}

        public static final String BASE_URL = "https://www.portalbank.no/";

        static final String LOGIN_URL =
                "https://id.portalbank.no/web-kundeid/webresources/identifiser/eika/0770?returnUrl=https%3a%2f%2feika.no%2flogin%3freturnUrl%3d%2foversikt";
    }

    private static final String KONTOER = "nettbank2/kontoer/";
    public static final String MINE_KONTOER_PATH = KONTOER + "mine_kontoer/";
    public static final String KONTOBEVEGELSER_PATH = KONTOER + "kontobevegelser/?accountId=";

    private static final String API_VER = "servlet/restapi/0001/";
    public static final String USER_AGREEMENT_PATH = API_VER + "user/agreement";
    public static final String ACCOUNTS_PATH = API_VER + "accounts/list/filter";
    public static final String ACCOUNTS_TRANSACTION_PATH = API_VER + "accounts/transactions/search";

    public static class ErrorMessages {
        private ErrorMessages() {}

        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
    }

    public static class Headers {
        private Headers() {}

        public static final String X_SDC_LOCALE = "X-SDC-LOCALE";
        public static final String X_SDC_CLIENT_TYPE = "X-SDC-CLIENT-TYPE";
        public static final String X_SDC_API_VERSION = "X-SDC-API-VERSION";
        public static final String USER_AGENT = "User-Agent";
        public static final String USER_AGENT_VALUE =
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36";

        public static final String LOCALE_EN = "en";
        public static final String CLIENT_TYPE = "web";
        public static final String API_VERSION_1 = "1";
        public static final String API_VERSION_2 = "2";
        public static final String API_VERSION_3 = "3";
    }
}
