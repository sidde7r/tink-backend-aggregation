package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.TransactionalAccountFixtures;

public class AccountEntityTest {

    @Test
    public void shouldBeSwitchedOutAccount() {
        // given
        AccountEntity account = TransactionalAccountFixtures.switchedOutAccount();

        // then
        assertThat(account.isNotSwitchedOutAccount()).isFalse();
    }

    @Test
    public void shouldBeNotSwitchedOutAccount() {
        // given
        AccountEntity account = TransactionalAccountFixtures.currentAccount();

        // then
        assertThat(account.isNotSwitchedOutAccount()).isTrue();
    }

    @Test
    public void shouldHasEmptyAccountId() {
        // given
        AccountEntity account = TransactionalAccountFixtures.currentAccountWithEmptyAccountId();

        // then
        assertThat(account.hasAccountId()).isFalse();
    }

    @Test
    public void shouldHasAccountId() {
        // given
        AccountEntity account = TransactionalAccountFixtures.currentAccount();

        // then
        assertThat(account.hasAccountId()).isTrue();
    }
}
