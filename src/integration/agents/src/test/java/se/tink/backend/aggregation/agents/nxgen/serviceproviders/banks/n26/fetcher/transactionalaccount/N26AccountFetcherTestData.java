package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.transactionalaccount;

import java.io.File;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.SavingsAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.SavingsSpaceResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class N26AccountFetcherTestData {

    private static final String fetchAccountsResponseFilePath =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/n26/resources/fetch_accounts_response.json";

    private static final String fetchSavingsAccountsResponseFilePath =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/n26/resources/fetch_savings_accounts_response.json";

    private static final String fetchSavingsSpacesResponseFilePath =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/n26/resources/fetch_savings_spaces_response.json";

    public static AccountResponse fetchAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                new File(fetchAccountsResponseFilePath), AccountResponse.class);
    }

    static SavingsAccountResponse fetchSavingsAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                new File(fetchSavingsAccountsResponseFilePath), SavingsAccountResponse.class);
    }

    static SavingsSpaceResponse fetchSpaceSavingsAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                new File(fetchSavingsSpacesResponseFilePath), SavingsSpaceResponse.class);
    }
}
