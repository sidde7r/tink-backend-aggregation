package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.newday;

public final class NewDayConstants {
    public static final String ORGANISATION_ID = "0015800001ZEc2SAAT";
    public static final String AIS_API_URL = "https://api.newdaycards.com/open-banking/v3.1/aisp";
    private static final String WELL_KNOWN_URL =
            "https://api.newdaycards.com/identity/v1.0/%s/.well-known/openid-configuration";

    public static String getWellKnownUrlByBrand(final String brand) {
        return String.format(WELL_KNOWN_URL, brand);
    }
}
