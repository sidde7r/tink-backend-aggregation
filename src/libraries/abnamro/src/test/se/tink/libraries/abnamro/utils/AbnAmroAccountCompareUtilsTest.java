package se.tink.libraries.abnamro.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import se.tink.backend.core.Account;
import static org.assertj.core.api.Assertions.assertThat;

public class AbnAmroAccountCompareUtilsTest {
    @Test
    public void testEmptyInputs() {
        List<Account> accounts = ImmutableList.of();
        Set<String> active = ImmutableSet.of();
        Set<String> inactive = ImmutableSet.of();

        AbnAmroAccountCompareUtils comparer = new AbnAmroAccountCompareUtils(accounts, active, inactive);

        AbnAmroAccountCompareUtils.Result result = comparer.compare();

        assertThat(result.getMissingAtAbnAmro()).isEmpty();
        assertThat(result.getMissingAtTink()).isEmpty();
        assertThat(result.getActiveAtAbnAmroInactiveAtTink()).isEmpty();
        assertThat(result.getActiveAtTinkInactiveAtAbnAmro()).isEmpty();
    }

    @Test
    public void testMissingAtAbnAmro() {
        Account account = new Account();
        account.setBankId("123");

        List<Account> accounts = ImmutableList.of(account);
        Set<String> active = ImmutableSet.of();
        Set<String> inactive = ImmutableSet.of();

        AbnAmroAccountCompareUtils comparer = new AbnAmroAccountCompareUtils(accounts, active, inactive);

        AbnAmroAccountCompareUtils.Result result = comparer.compare();

        assertThat(result.getMissingAtAbnAmro()).containsExactly("123");
        assertThat(result.getMissingAtTink()).isEmpty();
        assertThat(result.getActiveAtAbnAmroInactiveAtTink()).isEmpty();
        assertThat(result.getActiveAtTinkInactiveAtAbnAmro()).isEmpty();
    }

    @Test
    public void testMissingAtTink() {
        List<Account> accounts = ImmutableList.of();
        Set<String> active = ImmutableSet.of("123");
        Set<String> inactive = ImmutableSet.of();

        AbnAmroAccountCompareUtils comparer = new AbnAmroAccountCompareUtils(accounts, active, inactive);

        AbnAmroAccountCompareUtils.Result result = comparer.compare();

        assertThat(result.getMissingAtAbnAmro()).isEmpty();
        assertThat(result.getMissingAtTink()).containsExactly("123");
        assertThat(result.getActiveAtAbnAmroInactiveAtTink()).isEmpty();
        assertThat(result.getActiveAtTinkInactiveAtAbnAmro()).isEmpty();
    }

    @Test
    public void testActiveAtTinkInactiveAtAbnAmro() {
        Account account = new Account();
        account.setBankId("123");

        List<Account> accounts = ImmutableList.of(account);
        Set<String> active = ImmutableSet.of();
        Set<String> inactive = ImmutableSet.of("123");

        AbnAmroAccountCompareUtils comparer = new AbnAmroAccountCompareUtils(accounts, active, inactive);

        AbnAmroAccountCompareUtils.Result result = comparer.compare();

        assertThat(result.getMissingAtAbnAmro()).isEmpty();
        assertThat(result.getMissingAtTink()).isEmpty();
        assertThat(result.getActiveAtAbnAmroInactiveAtTink()).isEmpty();
        assertThat(result.getActiveAtTinkInactiveAtAbnAmro()).containsExactly("123");
    }

    @Test
    public void testInactiveAtTinkActiveAtAbnAmro() {
        Account account = new Account();
        account.setBankId("123");

        AbnAmroUtils.markAccountAsRejected(account, 2);

        List<Account> accounts = ImmutableList.of(account);
        Set<String> active = ImmutableSet.of("123");
        Set<String> inactive = ImmutableSet.of();

        AbnAmroAccountCompareUtils comparer = new AbnAmroAccountCompareUtils(accounts, active, inactive);

        AbnAmroAccountCompareUtils.Result result = comparer.compare();

        assertThat(result.getMissingAtAbnAmro()).isEmpty();
        assertThat(result.getMissingAtTink()).isEmpty();
        assertThat(result.getActiveAtAbnAmroInactiveAtTink()).containsExactly("123");
        assertThat(result.getActiveAtTinkInactiveAtAbnAmro());
    }

    @Test
    public void testMultipleInputs() {
        Account activeAccount = new Account();
        activeAccount.setBankId("activeTink");

        Account inactiveAccount = new Account();
        inactiveAccount.setBankId("inactiveTink");

        Account missingAccount = new Account();
        missingAccount.setBankId("missingAbnAmro");

        AbnAmroUtils.markAccountAsRejected(inactiveAccount, 2);

        List<Account> accounts = ImmutableList.of(activeAccount, inactiveAccount, missingAccount);
        Set<String> active = ImmutableSet.of("activeTink", "missingActiveTink");
        Set<String> inactive = ImmutableSet.of("inactiveTink", "missingInactiveTink");

        AbnAmroAccountCompareUtils comparer = new AbnAmroAccountCompareUtils(accounts, active, inactive);

        AbnAmroAccountCompareUtils.Result result = comparer.compare();

        assertThat(result.getMissingAtAbnAmro()).containsExactly("missingAbnAmro");
        assertThat(result.getMissingAtTink()).containsExactly("missingActiveTink", "missingInactiveTink");
        assertThat(result.getActiveAtAbnAmroInactiveAtTink()).isEmpty();
        assertThat(result.getActiveAtTinkInactiveAtAbnAmro()).isEmpty();
    }
}
