package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import se.tink.libraries.serialization.utils.SerializationUtils;

public class GetAccountsResponseTestData {

    public static GetAccountsResponse getTestData() {
        return SerializationUtils.deserializeFromString(
                BankdataConstantsTestData.ACCOUNTS_RESPONSE_DATA, GetAccountsResponse.class);
    }
}
