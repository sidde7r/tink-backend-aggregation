package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.responsecodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.transactional.detail.AccountsTestData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.GetAccountsResponse;

public class GetAccountsResponseTest {

    @Test
    public void shouldReportSuccessIfSuccessfulGetAccountsResponse() {
        GetAccountsResponse response =
                AccountsTestData.getResponse(AccountsTestData.PAYLOAD_ACCOUNT_ID_1);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldReportFailureIfUnsuccessfulGetAccountsResponse() {
        GetAccountsResponse response =
                AccountsTestData.getResponse(AccountsTestData.PAYLOAD_ERRORED);
        assertFalse(response.isSuccessful());
    }
}
