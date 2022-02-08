package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.tsb;

final class TsbConstants {

    public static final String ORGANISATION_ID = "0015800001ZEZ3hAAH";

    public static final String AIS_API_URL = "https://apis.tsb.co.uk/apis/open-banking/v3.1/aisp";
    public static final String PIS_API_URL = "https://apis.tsb.co.uk/apis/open-banking/v3.1/pisp";
    public static final String WELL_KNOWN_URL =
            "https://apis.tsb.co.uk/apis/open-banking/v3.1/.well-known/openid-configuration";

    public static final int TIMEOUT_LIMIT_MS = 60000;
}
