package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays;

public final class BarclaysConstants {

    public static final String ORGANISATION_ID = "0015800000jfAW1AAM";

    public static final String AIS_API_URL =
            "https://telesto.api.barclays:443/open-banking/v3.1/aisp";
    public static final String PIS_API_URL =
            "https://telesto.api.barclays:443/open-banking/v3.1/pisp";
    public static final String PERSONAL_WELL_KNOWN_URL =
            "https://oauth.tiaa.barclays.com/BarclaysPersonal/.well-known/openid-configuration";
    public static final String BUSINESS_WELL_KNOWN_URL =
            "https://oauth.tiaa.barclays.com/BarclaysBusiness/.well-known/openid-configuration";
    public static final String CORPORATE_WELL_KNOWN_URL =
            "https://oauth.tiaa.barclays.com/BarclaysCorporate/.well-known/openid-configuration";
    public static final String BARCLAYCARD_BUSINESS_WELL_KNOWN_URL =
            "https://oauth.tiaa.barclays.com/BCP/.well-known/openid-configuration";
}
