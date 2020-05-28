package se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.rules.regulatory.psd2;

import com.google.common.collect.ImmutableList;
import se.tink.libraries.enums.MarketCode;

public class Psd2Markets {
    // This list contain (justification):
    //  - EU countries (PSD2)
    //  - UK (Aligned with PSD2 regulations)
    //  - NO (Aligned with PSD2 regulations)
    private static final ImmutableList<MarketCode> PSD2_MARKETS =
            ImmutableList.<MarketCode>builder()
                    // EU countries
                    .add(MarketCode.AT)
                    .add(MarketCode.BE)
                    .add(MarketCode.BG)
                    .add(MarketCode.HR)
                    .add(MarketCode.CY)
                    .add(MarketCode.CZ)
                    .add(MarketCode.DK)
                    .add(MarketCode.EE)
                    .add(MarketCode.FI)
                    .add(MarketCode.FR)
                    .add(MarketCode.DE)
                    .add(MarketCode.GR)
                    .add(MarketCode.HU)
                    .add(MarketCode.IT)
                    .add(MarketCode.LV)
                    .add(MarketCode.LT)
                    .add(MarketCode.LU)
                    .add(MarketCode.MT)
                    .add(MarketCode.NL)
                    .add(MarketCode.PL)
                    .add(MarketCode.PT)
                    .add(MarketCode.RO)
                    .add(MarketCode.SI)
                    .add(MarketCode.ES)
                    .add(MarketCode.SE)

                    // UK
                    .add(MarketCode.IE)
                    .add(MarketCode.GB)
                    .add(MarketCode.UK)

                    // Norway
                    .add(MarketCode.NO)
                    .build();

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
