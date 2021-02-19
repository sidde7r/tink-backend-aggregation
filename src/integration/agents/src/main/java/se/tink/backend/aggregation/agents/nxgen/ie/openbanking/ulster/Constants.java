package se.tink.backend.aggregation.agents.nxgen.ie.openbanking.ulster;

final class Constants {

    private Constants() {}

    public static final String ORG_ID = "0015800001ZEZ1iAAH";

    public static final String AIS_API_URL = "https://api.ulsterbank.ie/open-banking/v3.1/aisp";

    public static final String PERSONAL_WELL_KNOWN_URL =
            "https://personal.secure1.ulsterbank.ie/.well-known/openid-configuration";
    public static final String CORPORATE_WELL_KNOWN_URL =
            "https://corporate.secure1.ulsterbank.ie/.well-known/openid-configuration";
}
