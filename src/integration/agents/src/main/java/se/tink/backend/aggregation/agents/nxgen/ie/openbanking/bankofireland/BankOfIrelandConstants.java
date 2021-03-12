package se.tink.backend.aggregation.agents.nxgen.ie.openbanking.bankofireland;

public final class BankOfIrelandConstants {
    public static final String ORGANISATION_ID = "0015800001ZEc2UAAT";

    public static final String AIS_API_URL =
            "https://api.ob.bankofireland.com/1/api/open-banking/v3.0/aisp";
    public static final String BUSINESS_WELL_KNOWN_URL =
            "https://auth.ob.bankofireland.com/oauth/as/bol/.well-known/openid-configuration";
    public static final String PERSONAL_WELL_KNOWN_URL =
            "https://auth.ob.bankofireland.com/oauth/as/b365/.well-known/openid-configuration";
}
