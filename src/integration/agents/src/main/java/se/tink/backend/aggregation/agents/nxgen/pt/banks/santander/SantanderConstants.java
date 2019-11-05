package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander;

import java.time.format.DateTimeFormatter;

public class SantanderConstants {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final String TIMEZONE_ID = "Portugal";
    public static final String API_URL = "https://m.santandertotta.pt/gatewayios/Gateway.aspx";

    public static class STORAGE {
        public static final String SESSION_TOKEN = "SESSION_TOKEN";
        public static final String CURRENCY_CODE = "CURRENCY_CODE";
        public static final String BRANCH_CODE = "BRANCH_CODE";
    }

    public static class RESPONSE_CODES {
        public static final String OK = "0";
        public static final String ERROR = "1";
        public static final String SESSION_EXPIRED = "5";
    }
}
