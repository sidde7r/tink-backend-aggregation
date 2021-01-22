package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class ArgentaFetcherTestData {
    static final AccountResponse ACCOUNT_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"accounts\":[{\"iban\": \"IBAN\", \"bban\": \"BBAN\", \"product\": \"PRODUCT\", \"resourceId\": \"RESOURCE_ID\", \"balances\": [{\"balanceAmount\": {\"amount\": \"6.66\",\"currency\": \"EUR\"}} ], \"cashAccountType\": \"CACC\",\"_links\": {\"balances\": { \"href\": \"HREF\"}, \"transactions\": {\"href\":\"HREF1\"}}}]}",
                    AccountResponse.class);

    static final TransactionsResponse TRANSACTIONS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"_links\" : {} , \"transactions\" : { \"booked\" : [{ \"valueDate\" : \"2000-10-10\", \"remittanceInformationUnstructured\" : \"DESCRIPTION\", \"transactionAmount\" : { \"amount\" : \"6.66\" , \"currency\" : \"EUR\"} }], \"pending\" : [{\"valueDate\" : \"2000-10-10\", \"remittanceInformationUnstructured\" : \"DESCRIPTION\", \"transactionAmount\" : { \"amount\" : \"6.66\" , \"currency\" : \"EUR\"}}]} }",
                    TransactionsResponse.class);
}
