package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static se.tink.backend.aggregation.agents.nxgen.nl.banks.ics.TestHelper.ACCOUNT_ID;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.BalanceDataEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditBalanceResponse;

public class CreditBalanceResponseTest {

    private CreditBalanceResponse balanceResponse;

    @Test
    public void shouldThrowMissingBalanceWhenThereWillBeNoMatchingAccountId() {
        // given
        balanceResponse = TestHelper.getEmptyCreditBalanceResponse();

        // then
        assertThatThrownBy(() -> balanceResponse.getBalance(ACCOUNT_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No balance available");
    }

    @Test
    public void shouldMapResponseToBalanceEntity() {
        // given
        balanceResponse = TestHelper.getCreditBalanceResponse();
        BalanceDataEntity balanceDataEntity = TestHelper.getBalanceDataEntity();
        // then
        assertThat(balanceResponse.getBalance(ACCOUNT_ID))
                .usingRecursiveComparison()
                .isEqualTo(balanceDataEntity.getBalance().get(0));
    }
}
