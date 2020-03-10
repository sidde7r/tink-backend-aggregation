package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class SdcNoConstants {
    public static class Market {
        public static final String BASE_URL = "https://prod.smartno.sdc.dk/restapi/";
        public static final String EIKA_BASE_URL = "https://prod-smarteika.portalbank.no/restapi/";
        public static final String PHONE_COUNTRY_CODE = "+47";
        public static final ImmutableList<String> EIKA_BANKS = ImmutableList.of("2230");
    }

    public static class Logging {
        public static final LogTag LOAN_TAG = LogTag.from("#loan_logging_sdc_no");
        public static final LogTag INVESTMENTS_TAG = LogTag.from("#investment_logging_sdc_no");
    }

    public static class Authentication {
        private static final String CULTURA_BANK = "1254";
        private static final String EASY_BANK = "9791";
        private static final String PERSONELLSERVICE_TRONDELAG = "0010";

        // test Storebrand does not support SMS pinning of devices
        public static final ImmutableList<String> BANKS_WITH_IFRAME_BANKID_AUTHENTICATION =
                ImmutableList.of(CULTURA_BANK, EASY_BANK, PERSONELLSERVICE_TRONDELAG);

        public static final String IFRAME_BANKID_LOGIN_URL =
                "https://www.nettbankportal.no/{bankcode}/nettbank2/logon/bankidjs/";
    }
}
