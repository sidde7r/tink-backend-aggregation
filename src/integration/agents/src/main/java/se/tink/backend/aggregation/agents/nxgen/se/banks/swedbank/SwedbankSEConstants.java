package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileParameters;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;

public class SwedbankSEConstants {
    public static final String LOAN_YEARS = "år";
    public static final String LOAN_MONTHS = "mån";
    public static final String MEMBERSHIP_LOAN = "Medlemslån";
    public static final String AMORTIZATION = "Amorteringsbelopp";

    public static final String HOST = "https://auth.api.swedbank.se/TDE_DAP_Portal_REST_WEB/api";

    /*
     * Updating API key and User Agent:
     * The apps don't do certificate pinning, so any phone can be used. It is not necessary to log in.
     * apiKey: Encoded in `Authorization` header. Header is base64(apiKey + ":" + device ID).
     * userAgent: `User-Agent` header.
     */

    public static final ImmutableMap<String, ProfileParameters> PROFILE_PARAMETERS =
            new ImmutableMap.Builder<String, ProfileParameters>()
                    .put(
                            "swedbank",
                            new ProfileParameters(
                                    "swedbank",
                                    "XZoIoix8H434kLTZ",
                                    false,
                                    "SwedbankMOBPrivateIOS/7.34.0_(iOS;_14.7.1)_Apple/iPhone13,2"))
                    .put(
                            "swedbank-business",
                            new ProfileParameters(
                                    "swedbank-business",
                                    "SSAigLBubEiyKwyQ",
                                    false,
                                    "SwedbankMOBCorporateIOS/3.20.0_(iOS;_14.7.1)_Apple/iPhone13,2"))
                    .put(
                            "savingsbank",
                            new ProfileParameters(
                                    "savingsbank",
                                    "YMS3kPl5Grb32boY",
                                    true,
                                    "SavingbankMOBPrivateIOS/7.34.0_(iOS;_14.4.2)_Apple/iPhone10,4"))
                    .put(
                            "savingsbank-business",
                            new ProfileParameters(
                                    "savingsbank-business",
                                    "lYIPcFLK5izCCQL3",
                                    true,
                                    "SavingbankMOBCorporateIOS/3.20.0_(iOS;_14.4.2)_Apple/iPhone10,4"))
                    .build();

    public static final TypeMapper<InstrumentModule.InstrumentType> INSTRUMENT_TYPE_MAP =
            TypeMapper.<InstrumentModule.InstrumentType>builder()
                    .put(InstrumentModule.InstrumentType.FUND, "FUND")
                    .put(InstrumentModule.InstrumentType.OTHER, "EQUITY")
                    .setDefaultTranslationValue(InstrumentModule.InstrumentType.OTHER)
                    .build();

    public static class StorageKey {
        public static final String HAS_EXTENDED_USAGE = "hasExtendedUsage";
    }

    public static class Endpoint {
        public static final String TRANSACTIONS_BASE = "/v5/engagement/transactions";
    }

    public static class HeaderKeys {
        public static final String X_CLIENT = "X-Client";
        public static final String ADRUM = "ADRUM";
        public static final String ADRUM_1 = "ADRUM_1";
    }

    public static class HeaderValues {
        public static final String ADRUM = "isAjax:true";
        public static final String ADRUM_1 = "isMobile:true";
    }
}
