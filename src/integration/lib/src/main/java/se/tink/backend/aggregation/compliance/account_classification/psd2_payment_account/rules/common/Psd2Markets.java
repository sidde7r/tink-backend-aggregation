package se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.rules.common;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import se.tink.libraries.enums.MarketCode;

public class Psd2Markets {
    // This list contain (justification):
    //  - EU countries (PSD2)
    //  - UK (Aligned with PSD2 regulations)
    //  - NO (Aligned with PSD2 regulations)
    @VisibleForTesting
    static final ImmutableSet<MarketCode> PSD2_MARKETS =
            ImmutableSet.of(
                    // EU countries
                    MarketCode.AT,
                    MarketCode.BE,
                    MarketCode.BG,
                    MarketCode.HR,
                    MarketCode.CY,
                    MarketCode.CZ,
                    MarketCode.DK,
                    MarketCode.EE,
                    MarketCode.FI,
                    MarketCode.FR,
                    MarketCode.DE,
                    MarketCode.GR,
                    MarketCode.HU,
                    MarketCode.IT,
                    MarketCode.LV,
                    MarketCode.LT,
                    MarketCode.LU,
                    MarketCode.MT,
                    MarketCode.NL,
                    MarketCode.PL,
                    MarketCode.PT,
                    MarketCode.RO,
                    MarketCode.SI,
                    MarketCode.ES,
                    MarketCode.SE,

                    // UK
                    MarketCode.IE,
                    MarketCode.GB,
                    MarketCode.UK,

                    // Norway
                    MarketCode.NO);

    public static boolean isPsd2Market(MarketCode marketCode) {
        return PSD2_MARKETS.contains(marketCode);
    }

    public static boolean isPsd2Market(String marketCodeAsString) {
        try {
            MarketCode marketCode = MarketCode.valueOf(marketCodeAsString);
            return isPsd2Market(marketCode);
        } catch (NullPointerException | IllegalArgumentException e) {
            return false;
        }
    }
}
