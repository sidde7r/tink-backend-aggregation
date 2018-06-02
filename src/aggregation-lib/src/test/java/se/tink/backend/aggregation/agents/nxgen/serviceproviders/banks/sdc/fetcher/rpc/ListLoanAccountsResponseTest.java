package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcLoanAccount;
import static java.util.Optional.empty;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ListLoanAccountsResponseTest {

    @Test
    public void getTinkAccountsLoan() throws Exception {
        ListLoanAccountsResponse response = ListLoanAccountsResponseTestData.getTestData();
        assertTrue(response.size() == 3);

        assertNotNull(response);

        for (SdcLoanAccount loanAccount : response) {
            assertNotNull(loanAccount.getLabel());
            assertTrue(0 > loanAccount.getAmount().toTinkAmount().getValue());
            assertThat(loanAccount.findAccountId(), not(empty()));
        }
    }
}
