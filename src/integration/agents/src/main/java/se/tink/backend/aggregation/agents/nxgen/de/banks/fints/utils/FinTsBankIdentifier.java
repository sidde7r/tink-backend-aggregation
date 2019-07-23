package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils;

import com.google.common.collect.ImmutableBiMap;

public class FinTsBankIdentifier {
    private static final ImmutableBiMap<String, String> COUNTRY_NUMBER_MAP =
            new ImmutableBiMap.Builder<String, String>()
                    .put("BE", "056")
                    .put("BG", "100")
                    .put("DK", "208")
                    .put("DE", "280")
                    .put("FI", "246")
                    .put("FR", "250")
                    .put("GR", "300")
                    .put("GB", "826")
                    .put("IE", "372")
                    .put("IS", "352")
                    .put("IT", "380")
                    .put("JP", "392")
                    .put("CA", "124")
                    .put("HR", "191")
                    .put("LI", "438")
                    .put("LU", "442")
                    .put("NL", "528")
                    .put("AT", "040")
                    .put("PL", "616")
                    .put("PT", "620")
                    .put("RO", "642")
                    .put("RU", "643")
                    .put("SE", "752")
                    .put("CH", "756")
                    .put("SK", "703")
                    .put("SI", "705")
                    .put("ES", "724")
                    .put("CZ", "203")
                    .put("TR", "792")
                    .put("HU", "348")
                    .put("US", "840")
                    .put("EU", "978")
                    .build();

    public static String countryAlphaToNumeric(String alpha) {
        return COUNTRY_NUMBER_MAP.get(alpha);
    }

    public static String countryNumericToAlpha(String numeric) {
        return COUNTRY_NUMBER_MAP.inverse().get(numeric);
    }
}
