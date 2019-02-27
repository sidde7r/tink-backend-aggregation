package se.tink.backend.aggregation.agents.nxgen.se.banks.alandsbankenbankid;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public class AlandsBankenSEConstants {

    public static final class Url {
        public static final String BASE = "https://mob.alandsbanken.se/cbs-inet-json-api-abs-v1/api/";
    }

    public static final class Fetcher {
        public static final LogTag TRANSACTION_LOGGING = LogTag.from
                ("#transaction_alandsbank_se");
        public static final LogTag LOAN_LOGGING = LogTag.from("#loan_alandsbank_se");
        public static final LogTag INVESTMENT_PORTFOLIO_LOGGING = LogTag.from
                ("#investment_portfolio_alandsbank_se");
        public static final LogTag INVESTMENT_INSTRUMENT_LOGGING = LogTag.from
                ("#investment_instrument_alandsbank_se");
    }
}
