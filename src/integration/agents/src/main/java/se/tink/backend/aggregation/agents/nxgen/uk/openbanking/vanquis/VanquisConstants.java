package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.vanquis;

final class VanquisConstants {
    static final String ORGANIZATION_ID = "0015800001ZEc2PAAT";
    static final String AIS_API_URL =
            "https://mtls.data.openbanking.vanquis.co.uk/open-banking/v3.1/aisp";
    static final String WELL_KNOWN_URL =
            "https://auth.openbanking.vanquis.co.uk/.well-known/openid-configuration";

    private VanquisConstants() {
        throw new UnsupportedOperationException();
    }
}
