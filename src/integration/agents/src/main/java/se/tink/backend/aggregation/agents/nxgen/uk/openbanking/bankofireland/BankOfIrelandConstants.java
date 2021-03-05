package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofireland;

public final class BankOfIrelandConstants {
    public static final String ORGANISATION_ID = "0015800000jfQ9aAAE";

    public static final String AIS_API_URL =
            "https://api.obapi.bankofireland.com/1/api/open-banking/v3.0/aisp";
    public static final String BUSINESS_WELL_KNOWN_URL =
            "https://auth.obapi.bankofireland.com/oauth/as/bol/.well-known/openid-configuration";
    public static final String PERSONAL_WELL_KNOWN_URL =
            "https://auth.obapi.bankofireland.com/oauth/as/b365/.well-known/openid-configuration";
}
