package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.accounts.HandelsbankenUkAccountConverter;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class HandelsbankenUkAccountConverterTest {

    private static final HolderName EXPECTED_HOLDER_NAME = new HolderName("Fluffy");
    private static final AccountIdentifier EXPECTED_ACCOUNT_ID =
            AccountIdentifier.create(
                    AccountIdentifierType.IBAN, "GB06HAND40516249747837", "FLUFFY");

    private HandelsbankenUkAccountConverter accountConverter;

    @Before
    public void setup() {
        accountConverter = new HandelsbankenUkAccountConverter();
    }

    @Test
    public void shouldParseCheckingAccount() throws Exception {
        // given
        AccountsItemEntity account = HandelsbankenUkFixtures.checkingAccount();
        AccountDetailsResponse details = HandelsbankenUkFixtures.allBalances();

        // when
        TransactionalAccount result = accountConverter.toTinkAccount(account, details).get();

        // then
        assertEquals(AccountTypes.CHECKING, result.getType());
        assertEquals(EXPECTED_ACCOUNT_ID, result.getIdentifiersAsList().get(0));
        assertEquals(EXPECTED_HOLDER_NAME, result.getHolderName());
    }

    @Test
    public void shouldParseCheckingAccountWithoutName() throws Exception {
        // given
        AccountsItemEntity account =
                HandelsbankenUkFixtures.checkingAccountWithoutProperAccountName();
        AccountDetailsResponse details = HandelsbankenUkFixtures.allBalances();

        // when
        TransactionalAccount result = accountConverter.toTinkAccount(account, details).get();

        // then
        assertEquals(AccountTypes.CHECKING, result.getType());
        assertEquals(EXPECTED_ACCOUNT_ID, result.getIdentifiersAsList().get(0));
        assertEquals(EXPECTED_HOLDER_NAME, result.getHolderName());
    }

    @Test
    public void shouldParseSavingsAccount() throws Exception {
        // given
        AccountsItemEntity account = HandelsbankenUkFixtures.savingsAccount();
        AccountDetailsResponse details = HandelsbankenUkFixtures.currentBalance();

        // when
        TransactionalAccount result = accountConverter.toTinkAccount(account, details).get();

        // then
        assertEquals(AccountTypes.SAVINGS, result.getType());
        assertEquals(EXPECTED_ACCOUNT_ID, result.getIdentifiersAsList().get(0));
        assertEquals(EXPECTED_HOLDER_NAME, result.getHolderName());
    }

    @Test
    public void shouldParseSavingsAccountWithoutName() throws Exception {
        // given
        AccountsItemEntity account =
                HandelsbankenUkFixtures.savingsAccountWithoutProperAccountName();
        AccountDetailsResponse details = HandelsbankenUkFixtures.currentBalance();

        // when
        TransactionalAccount result = accountConverter.toTinkAccount(account, details).get();

        // then
        assertEquals(AccountTypes.SAVINGS, result.getType());
        assertEquals(EXPECTED_ACCOUNT_ID, result.getIdentifiersAsList().get(0));
        assertEquals(EXPECTED_HOLDER_NAME, result.getHolderName());
    }

    @Test
    public void shouldThrowBalanceException() throws Exception {
        // given
        AccountsItemEntity account = HandelsbankenUkFixtures.savingsAccount();
        AccountDetailsResponse details = HandelsbankenUkFixtures.noBalances();

        // when
        Throwable throwable =
                catchThrowable(() -> accountConverter.toTinkAccount(account, details));

        // then
        assertThat(throwable)
                .isInstanceOf(AccountRefreshException.class)
                .hasMessage(ExceptionMessages.BALANCE_NOT_FOUND);
    }
}
