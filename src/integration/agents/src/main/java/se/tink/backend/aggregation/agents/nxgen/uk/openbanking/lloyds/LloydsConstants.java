package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.lloyds;

final class LloydsConstants {

    public static final String ORGANISATION_ID = "0015800000jf9GgAAI";

    public static final String AIS_API_URL =
            "https://secure-api.lloydsbank.com/prod01/lbg/lyds/open-banking/v3.1/aisp";
    public static final String PIS_API_URL =
            "https://secure-api.lloydsbank.com/prod01/lbg/lyds/open-banking/v3.1/pisp";
    public static final String WELL_KNOWN_PERSONAL_URL =
            "https://authorise-api.lloydsbank.co.uk/prod01/channel/lyds/personal/.well-known/openid-configuration";
    public static final String WELL_KNOWN_BUSINESS_URL =
            "https://authorise-api.lloydsbank.co.uk/prod01/channel/lyds/business/.well-known/openid-configuration";
    public static final String WELL_KNOWN_CORPORATE_URL =
            "https://authorise-api.lloydsbank.co.uk/prod01/channel/lyds/commercial/.well-known/openid-configuration";
}
