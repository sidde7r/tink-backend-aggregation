package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.accounts.HandelsbankenUkBalanceMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class HandelsbankenUkBalanceMapperTest {

    private HandelsbankenUkBalanceMapper balanceMapper;

    @Before
    public void setup() {
        balanceMapper = new HandelsbankenUkBalanceMapper();
    }

    @Test
    public void shouldParseCurrentAndAvailableBalance() throws Exception {
        // given
        AccountDetailsResponse response = HandelsbankenUkFixtures.allBalances();

        // when
        BalanceModule result = balanceMapper.createAccountBalance(response.getBalances());
        // then
        assertThat(result).isNotNull();
        assertThat(result.getExactAvailableBalance())
                .isEqualTo(ExactCurrencyAmount.of("90.09", "GBP"));
        assertThat(result.getExactBalance()).isEqualTo(ExactCurrencyAmount.of("90.09", "GBP"));
    }

    @Test
    public void shouldParseCurrentBalance() throws Exception {
        // given
        AccountDetailsResponse response = HandelsbankenUkFixtures.currentBalance();

        // when
        BalanceModule result = balanceMapper.createAccountBalance(response.getBalances());
        // then
        assertThat(result).isNotNull();
        assertNull(result.getExactAvailableBalance());
        assertThat(result.getExactBalance()).isEqualTo(ExactCurrencyAmount.of("90.09", "GBP"));
    }

    @Test
    public void shouldThrowExceptionWithNoBalance() throws Exception {
        // given
        AccountDetailsResponse response = HandelsbankenUkFixtures.noBalances();

        // when
        Throwable throwable =
                catchThrowable(() -> balanceMapper.createAccountBalance(response.getBalances()));
        // then
        assertThat(throwable)
                .isInstanceOf(AccountRefreshException.class)
                .hasMessage(ExceptionMessages.BALANCE_NOT_FOUND);
    }
}
