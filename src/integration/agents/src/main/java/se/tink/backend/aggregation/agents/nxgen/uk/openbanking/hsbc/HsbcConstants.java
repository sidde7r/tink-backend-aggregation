package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc;

final class HsbcConstants {

    public static final String ORGANISATION_ID = "00158000016i44JAAQ";

    public static final String PERSONAL_AIS_API_URL =
            "https://api.ob.hsbc.co.uk/obie/open-banking/v3.1/aisp";
    public static final String PERSONAL_PIS_API_URL =
            "https://api.ob.hsbc.co.uk/obie/open-banking/v3.1/pisp";
    public static final String PERSONAL_WELL_KNOWN_URL =
            "https://ob.hsbc.co.uk/.well-known/openid-configuration";

    public static final String BUSINESS_AIS_API_URL =
            "https://api.ob.business.hsbc.co.uk/obie/open-banking/v3.1/aisp";
    public static final String BUSINESS_WELL_KNOWN_URL =
            "https://api.ob.business.hsbc.co.uk/.well-known/openid-configuration";

    public static final String KINETIC_AIS_API_URL =
            "https://api.ob.hsbckinetic.com/obie/open-banking/v3.1/aisp";
    public static final String KINETIC_WELL_KNOWN_URL =
            "https://api.ob.hsbckinetic.co.uk/.well-known/openid-configuration";
}
