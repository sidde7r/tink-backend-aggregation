package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import se.tink.libraries.serialization.utils.SerializationUtils;

public class GetLoansResponseTestData {

    public static GetLoansResponse getTestData() {
        return SerializationUtils.deserializeFromString(
                BankdataConstantsTestData.ACCOUNTS_RESPONSE_DATA, GetLoansResponse.class);
    }
}
