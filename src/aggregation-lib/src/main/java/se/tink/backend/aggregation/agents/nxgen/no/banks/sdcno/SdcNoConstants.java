package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class SdcNoConstants {
    public static class Market {
        public static final String NORWAY = "NO";
        public static final String BASE_URL = "https://prod.smartno.sdc.dk/restapi/";
        public static final String PHONE_COUNTRY_CODE = "+47";
    }

    public static class Fetcher {
        public static final LogTag LOAN_LOGGING = LogTag.from("#loan_logging_sdc_no");
        public static final LogTag INVESTMENTS_LOGGING = LogTag.from("#investment_logging_sdc_no");
    }
}
