package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcLoanAccount;

public class ListLoanAccountsResponseTest {

    @Test
    public void getTinkAccountsLoan() {
        // given
        ListLoanAccountsResponse response = ListLoanAccountsResponseTestData.getTestData();

        // when && then
        for (SdcLoanAccount loanAccount : response) {
            assertThat(loanAccount.getAmount().toExactCurrencyAmount().getExactValue())
                    .isLessThan(BigDecimal.ZERO);
            assertThat(loanAccount.getLabel()).isNotNull();
            assertThat(loanAccount.findAccountId()).isNotNull();
            assertThat(loanAccount.findAccountId().get()).isNotEmpty();
        }
    }
}
