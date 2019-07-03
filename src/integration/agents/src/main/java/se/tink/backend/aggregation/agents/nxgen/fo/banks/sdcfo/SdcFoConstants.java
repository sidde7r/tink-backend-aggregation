package se.tink.backend.aggregation.agents.nxgen.fo.banks.sdcfo;

import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class SdcFoConstants {
    public static class Market {
        public static final String BASE_URL = "https://prod.smartfo.sdc.dk/restapi/";
        public static final String PHONE_COUNTRY_CODE = "+298";
    }

    public static class Fetcher {
        public static final LogTag LOAN_LOGGING = LogTag.from("#loan_logging_sdc_fo");
        public static final LogTag INVESTMENTS_LOGGING = LogTag.from("#investment_logging_sdc_fo");
    }
}
