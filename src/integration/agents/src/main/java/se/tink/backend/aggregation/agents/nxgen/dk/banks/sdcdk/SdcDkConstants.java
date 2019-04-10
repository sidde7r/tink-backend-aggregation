package se.tink.backend.aggregation.agents.nxgen.dk.banks.sdcdk;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class SdcDkConstants {

    public static class Market {
        public static final String DENMARK = "DK";
        public static final String BASE_URL = "https://prod.smartdk.sdc.dk/restapi/";
        public static final String PHONE_COUNTRY_CODE = "+45";
    }

    public static class Fetcher {
        public static final LogTag LOAN_LOGGING = LogTag.from("#loan_logging_sdc_dk");
        public static final LogTag INVESTMENTS_LOGGING = LogTag.from("#investment_logging_sdc_dk");
    }

    public static class Authentication {
        // SDC banks supporting SMS pinning of devices
        public static final ImmutableList<String> BANKS_WITH_SMS_AUTHENTICATION =
                ImmutableList.of("0400", "9090", "9335");
    }
}
