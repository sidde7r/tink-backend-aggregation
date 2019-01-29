package se.tink.libraries.account.enums;

import java.util.EnumSet;
import se.tink.libraries.enums.TinkFeature;

public enum AccountExclusion {
    AGGREGATION(
            EnumSet.of(
                    TinkFeature.AGGREGATION,
                    TinkFeature.CATEGORIZATION,
                    TinkFeature.PFM,
                    TinkFeature.INDEXING_TRANSACTIONS,
                    TinkFeature.PAYMENTS)),

    // PFM_AND_SEARCH is the equivalent of the, now deprecated, boolean flag called 'excluded'
    PFM_AND_SEARCH(
            EnumSet.of(
                    TinkFeature.CATEGORIZATION,
                    TinkFeature.PFM,
                    TinkFeature.INDEXING_TRANSACTIONS)),

    PFM_DATA(EnumSet.of(TinkFeature.CATEGORIZATION, TinkFeature.PFM)),

    NONE(EnumSet.noneOf(TinkFeature.class));

    public static final String DOCUMENTED = "AGGREGATION, PFM_AND_SEARCH, PFM_DATA, NONE";

    public EnumSet<TinkFeature> excludedFeatures;

    AccountExclusion(EnumSet<TinkFeature> excludedFeatures) {
        this.excludedFeatures = excludedFeatures;
    }
}
