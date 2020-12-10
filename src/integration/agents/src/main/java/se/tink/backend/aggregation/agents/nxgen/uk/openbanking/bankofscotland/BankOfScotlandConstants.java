package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofscotland;

final class BankOfScotlandConstants {

    static final String ORGANIZATION_ID = "0015800000jfPKvAAM";

    static final String AIS_API_URL =
            "https://secure-api.bankofscotland.co.uk/prod01/lbg/bos/open-banking/v3.1/aisp";
    static final String PIS_API_URL =
            "https://secure-api.bankofscotland.co.uk/prod01/lbg/bos/open-banking/v3.1/pisp";
    static final String APP_TO_APP_AUTH_URL =
            "https://authorise-api.bankofscotland.co.uk/prod01/lbg/bos/personal/oidc-api/v1.1/authorize";
    static final String WELL_KNOWN_URL_PERSONAL =
            "https://authorise-api.bankofscotland.co.uk/prod01/channel/bos/personal/.well-known/openid-configuration";
    static final String WELL_KNOWN_URL_BUSINESS =
            "https://authorise-api.bankofscotland.co.uk/prod01/channel/bos/business/.well-known/openid-configuration";
    static final String WELL_KNOWN_URL_COMMERCIAL =
            "https://authorise-api.bankofscotland.co.uk/prod01/channel/bos/commercial/.well-known/openid-configuration";
}
