package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.authenticator;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class BPostAuthenticatorTestFixtures {

    static final String expectedCleanedUrl =
            "https://the.actual.authorization.endpoint?state=SOME_STATE&redirect_uri=https%3A%2F%2Fredirect.url&scope=AIS%3AsomeConsentId&code_challenge=someCodeChallenge&response_type=code&code_challenge_method=S256";

    static ConsentResponse givenConsentResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"consentId\" : \"someConsentId\","
                        + "  \"_links\" : {\n"
                        + "    \"scaOAuth\" : {\n"
                        + "      \"href\" : \"https://url.for.authorization.endpoint\"\n"
                        + "    }\n"
                        + "  }\n"
                        + "}",
                ConsentResponse.class);
    }
}
