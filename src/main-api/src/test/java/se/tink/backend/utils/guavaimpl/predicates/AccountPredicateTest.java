package se.tink.backend.utils.guavaimpl.predicates;

import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountPredicateTest {

    @Test
    public void testIsCreditCardPredicate() {
        Account creditCardAccount = new Account();
        creditCardAccount.setType(AccountTypes.CREDIT_CARD);

        assertThat(AccountPredicate.IS_CREDIT_CARD_ACCOUNT.apply(creditCardAccount)).isTrue();

        Account checkingAccount = new Account();
        checkingAccount.setType(AccountTypes.CHECKING);

        assertThat(AccountPredicate.IS_CREDIT_CARD_ACCOUNT.apply(checkingAccount)).isFalse();

        Account noAccountTypeAccount = new Account();
        checkingAccount.setType(null);

        assertThat(AccountPredicate.IS_CREDIT_CARD_ACCOUNT.apply(noAccountTypeAccount)).isFalse();
    }

    @Test
    public void testIsIncluded() {

        Account excludedAccount = new Account();
        excludedAccount.setExcluded(true);

        assertThat(AccountPredicate.IS_INCLUDED.apply(excludedAccount)).isFalse();

        Account includedAccount = new Account();
        includedAccount.setExcluded(false);

        assertThat(AccountPredicate.IS_INCLUDED.apply(includedAccount)).isTrue();

    }

    @Test
    public void isShared_false_whenOwnershipEqualTo1() {
        Account account = new Account();
        account.setOwnership(1.0);

        assertThat(AccountPredicate.IS_SHARED_ACCOUNT.apply(account)).isFalse();
    }

    @Test
    public void isShared_true_whenOwnershipEqualTo05() {
        Account account = new Account();
        account.setOwnership(0.5);

        assertThat(AccountPredicate.IS_SHARED_ACCOUNT.apply(account)).isTrue();
    }

    @Test
    public void isShared_false_whenOwnershipCloseTo1() {
        Account account = new Account();
        account.setOwnership(0.9999);

        assertThat(AccountPredicate.IS_SHARED_ACCOUNT.apply(account)).isFalse();
    }
}
