package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.couttsandco;

final class CouttsAndCoConstants {
    static final String ORGANISATION_ID = "0015800000ti1PbAAI";

    static final String AIS_API_URL = "https://api.coutts.com/open-banking/v3.1/aisp";

    static final String WELL_KNOWN_URL =
            "https://secure1.coutts.com/.well-known/openid-configuration";

    private CouttsAndCoConstants() {
        throw new UnsupportedOperationException();
    }
}
