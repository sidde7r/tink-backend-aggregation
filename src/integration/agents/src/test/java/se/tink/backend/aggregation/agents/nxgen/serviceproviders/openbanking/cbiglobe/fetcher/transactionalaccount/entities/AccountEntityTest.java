package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AccountEntityTest {

    private AccountEntity accountEntity;

    @Before
    public void setup() {
        accountEntity = genAccountEntity("dummyCashAccountType");
    }

    @Test
    public void shouldMapBalancesForAllPossibleBalances() {
        // given
        GetBalancesResponse getBalancesResponse = givenAllPossibleBalances();
        // when
        TransactionalAccount transactionalAccount =
                accountEntity
                        .toTinkAccount(getBalancesResponse)
                        .orElseThrow(IllegalArgumentException::new);
        // then
        assertThat(transactionalAccount.getExactBalance())
                .isEqualByComparingTo(ExactCurrencyAmount.inEUR(1.00));
        assertThat(transactionalAccount.getExactAvailableBalance())
                .isEqualByComparingTo(ExactCurrencyAmount.inEUR(4.00));
    }

    @Test
    public void shouldMapBookedBalanceByPriority() {
        // given
        List<BalanceEntity> balances = new ArrayList<>();
        GetBalancesResponse getBalancesResponse = new GetBalancesResponse(balances);

        // when
        balances.add(givenInterimAvailableBalance());
        // then
        assertThat(
                        accountEntity
                                .toTinkAccount(getBalancesResponse)
                                .orElseThrow(IllegalArgumentException::new)
                                .getExactBalance())
                .isEqualByComparingTo(ExactCurrencyAmount.inEUR(4.00));

        // when
        balances.add(givenExpectedBalance());
        // then
        assertThat(
                        accountEntity
                                .toTinkAccount(getBalancesResponse)
                                .orElseThrow(IllegalArgumentException::new)
                                .getExactBalance())
                .isEqualByComparingTo(ExactCurrencyAmount.inEUR(3.00));

        // when
        balances.add(givenAuthorisedBalance());
        // then
        assertThat(
                        accountEntity
                                .toTinkAccount(getBalancesResponse)
                                .orElseThrow(IllegalArgumentException::new)
                                .getExactBalance())
                .isEqualByComparingTo(ExactCurrencyAmount.inEUR(2.50));

        // when
        balances.add(givenClosingBookedBalance());
        // then
        assertThat(
                        accountEntity
                                .toTinkAccount(getBalancesResponse)
                                .orElseThrow(IllegalArgumentException::new)
                                .getExactBalance())
                .isEqualByComparingTo(ExactCurrencyAmount.inEUR(2.00));

        // when
        balances.add(givenOpeningBookedBalance());
        // then
        assertThat(
                        accountEntity
                                .toTinkAccount(getBalancesResponse)
                                .orElseThrow(IllegalArgumentException::new)
                                .getExactBalance())
                .isEqualByComparingTo(ExactCurrencyAmount.inEUR(1.00));
    }

    @Test
    public void shouldMapAvailableBalanceByPriority() {
        // given
        List<BalanceEntity> balances = new ArrayList<>();
        // this should not matter for available balance,
        // but it is required to have at least 1 of booked balances available or mapping won't work
        balances.add(givenClosingBookedBalance());
        GetBalancesResponse getBalancesResponse = new GetBalancesResponse(balances);

        // when
        balances.add(givenForwardAvailableBalance());
        // then
        assertThat(
                        accountEntity
                                .toTinkAccount(getBalancesResponse)
                                .orElseThrow(IllegalArgumentException::new)
                                .getExactAvailableBalance())
                .isEqualByComparingTo(ExactCurrencyAmount.inEUR(5.00));

        // when
        balances.add(givenExpectedBalance());
        // then
        assertThat(
                        accountEntity
                                .toTinkAccount(getBalancesResponse)
                                .orElseThrow(IllegalArgumentException::new)
                                .getExactAvailableBalance())
                .isEqualByComparingTo(ExactCurrencyAmount.inEUR(3.00));

        // when
        balances.add(givenInterimAvailableBalance());
        // then
        assertThat(
                        accountEntity
                                .toTinkAccount(getBalancesResponse)
                                .orElseThrow(IllegalArgumentException::new)
                                .getExactAvailableBalance())
                .isEqualByComparingTo(ExactCurrencyAmount.inEUR(4.00));
    }

    @Test
    public void shouldMapAccountWhenAvailableBalanceNotPresent() {
        // given
        List<BalanceEntity> balances = new ArrayList<>();
        // this should not matter for available balance,
        // but it is required to have at least 1 of booked balances available or mapping won't work
        balances.add(givenClosingBookedBalance());
        GetBalancesResponse getBalancesResponse = new GetBalancesResponse(balances);

        // when
        Optional<TransactionalAccount> transactionalAccount =
                accountEntity.toTinkAccount(getBalancesResponse);

        // then
        assertThat(transactionalAccount).isPresent();
        assertThat(transactionalAccount.get().getExactAvailableBalance()).isNull();
    }

    @Test
    public void shouldThrowExceptionIfBookedBalanceNotPresent() {
        // given
        GetBalancesResponse getBalancesResponse = new GetBalancesResponse(new ArrayList<>());

        // when

        // then
        assertThatCode(() -> accountEntity.toTinkAccount(getBalancesResponse))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Balance cannot be found.");
    }

    private GetBalancesResponse givenAllPossibleBalances() {
        List<BalanceEntity> balances = new ArrayList<>();
        balances.add(givenOpeningBookedBalance());
        balances.add(givenClosingBookedBalance());
        balances.add(givenExpectedBalance());
        balances.add(givenInterimAvailableBalance());
        balances.add(givenForwardAvailableBalance());
        return new GetBalancesResponse(balances);
    }

    private BalanceEntity givenOpeningBookedBalance() {
        AmountEntity amountEntity = new AmountEntity("EUR", "1.00");
        return new BalanceEntity(amountEntity, "openingBooked");
    }

    private BalanceEntity givenClosingBookedBalance() {
        AmountEntity amountEntity = new AmountEntity("EUR", "2.00");
        return new BalanceEntity(amountEntity, "closingBooked");
    }

    private BalanceEntity givenAuthorisedBalance() {
        AmountEntity amountEntity = new AmountEntity("EUR", "2.50");
        return new BalanceEntity(amountEntity, "authorised");
    }

    private BalanceEntity givenExpectedBalance() {
        AmountEntity amountEntity = new AmountEntity("EUR", "3.00");
        return new BalanceEntity(amountEntity, "expected");
    }

    private BalanceEntity givenInterimAvailableBalance() {
        AmountEntity amountEntity = new AmountEntity("EUR", "4.00");
        return new BalanceEntity(amountEntity, "interimAvailable");
    }

    private BalanceEntity givenForwardAvailableBalance() {
        AmountEntity amountEntity = new AmountEntity("EUR", "5.00");
        return new BalanceEntity(amountEntity, "forwardAvailable");
    }

    @Test
    public void toTinkAccountShouldMapAccountToCheckingIfCashAccountTypeNull() {
        // given
        List<BalanceEntity> balances = new ArrayList<>();
        balances.add(givenClosingBookedBalance());
        GetBalancesResponse getBalancesResponse = new GetBalancesResponse(balances);
        AccountEntity accountEntityWithNullType = genAccountEntity(null);

        // when
        Optional<TransactionalAccount> transactionalAccount =
                accountEntityWithNullType.toTinkAccount(getBalancesResponse);
        // then
        assertThat(transactionalAccount.isPresent()).isTrue();
        assertThat(transactionalAccount.get().getAccountFlags())
                .contains(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        assertThat(transactionalAccount.get().getType()).isEqualTo(AccountTypes.CHECKING);
    }

    @Test
    public void
            toTinkAccountShouldMapAccountToSavingIfCashAccountTypeEqualsToSavingAccountTypeEnum() {
        // given
        List<BalanceEntity> balances = new ArrayList<>();
        balances.add(givenClosingBookedBalance());
        GetBalancesResponse getBalancesResponse = new GetBalancesResponse(balances);
        AccountEntity savingAccountEntity = genAccountEntity("SVGS");

        // when
        Optional<TransactionalAccount> transactionalAccount =
                savingAccountEntity.toTinkAccount(getBalancesResponse);
        // then
        assertThat(transactionalAccount.isPresent()).isTrue();
        assertThat(transactionalAccount.get().getAccountFlags())
                .contains(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        assertThat(transactionalAccount.get().getType()).isEqualTo(AccountTypes.SAVINGS);
    }

    @Test
    public void
            toTinkAccountShouldMapAccountToCheckingIfCashAccountTypeEqualsToCheckingAccountTypeEnum() {
        // given
        List<BalanceEntity> balances = new ArrayList<>();
        balances.add(givenClosingBookedBalance());
        GetBalancesResponse getBalancesResponse = new GetBalancesResponse(balances);
        AccountEntity checkingAccountEntity = genAccountEntity("CASH");

        // when
        Optional<TransactionalAccount> transactionalAccount =
                checkingAccountEntity.toTinkAccount(getBalancesResponse);
        // then
        assertThat(transactionalAccount.isPresent()).isTrue();
        assertThat(transactionalAccount.get().getAccountFlags())
                .contains(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        assertThat(transactionalAccount.get().getType()).isEqualTo(AccountTypes.CHECKING);
    }

    public AccountEntity genAccountEntity(String cashAccountType) {
        return new AccountEntity("123", "456", "asdf", "someName", cashAccountType);
    }
}
