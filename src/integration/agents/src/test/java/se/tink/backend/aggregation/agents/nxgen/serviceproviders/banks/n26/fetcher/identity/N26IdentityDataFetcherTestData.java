package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.identity;

import java.io.File;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.MeResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class N26IdentityDataFetcherTestData {

    private static final String fetchIdentityResponseFilePath =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/n26/resources/fetch_identity_response.json";

    static MeResponse fetchIdentityResponse() {
        return SerializationUtils.deserializeFromString(
                new File(fetchIdentityResponseFilePath), MeResponse.class);
    }
}
