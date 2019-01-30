package se.tink.backend.aggregation.agents.banks.seb;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountUtilsTest {
    @Test
    public void ensureCorrectBankId_whenGettingBankIdBySubAccount() {
        Account account = createAccount("bankId-1", false, false);

        AccountUtils.setSubAccounts(account, Collections.singleton("subAccount-1"));
        assertThat(AccountUtils.getBankIdBySubAccount(Collections.singletonList(account), "subAccount-1"))
                .isEqualTo("bankId-1");
    }

    @Test
    public void ensureCorrectBankId_whenGettingBankIdBySubAccount_forMultipleAccounts() {
        Account account1 = createAccount("bankId-1", false, false);
        Account account2 = createAccount("bankId-2", false, false);

        AccountUtils.setSubAccounts(account1, Collections.singleton("subAccount-1"));
        AccountUtils.setSubAccounts(account2, Collections.singleton("subAccount-2"));

        assertThat(AccountUtils.getBankIdBySubAccount(ImmutableList.of(account1, account2), "subAccount-1"))
                .isEqualTo("bankId-1");
        assertThat(AccountUtils.getBankIdBySubAccount(ImmutableList.of(account1, account2), "subAccount-2"))
                .isEqualTo("bankId-2");
    }

    @Test
    public void ensureCorrectBankId_whenGettingBankIdBySubAccount_forSingleAccount_withMultipleSubAccounts() {
        Account account = createAccount("bankId-1", false, false);

        AccountUtils.setSubAccounts(account, ImmutableSet.of("subAccount-1", "subAccount-2"));
        assertThat(AccountUtils.getBankIdBySubAccount(Collections.singletonList(account), "subAccount-1"))
                .isEqualTo("bankId-1");
        assertThat(AccountUtils.getBankIdBySubAccount(Collections.singletonList(account), "subAccount-2"))
                .isEqualTo("bankId-1");
    }

    @Test
    public void ensureCorrectBankId_whenGettingBankIdBySubAccount_forMultipleAccounts_withMultipleSubAccounts() {
        Account account1 = createAccount("bankId-1", false, false);
        Account account2 = createAccount("bankId-2", false, false);

        AccountUtils.setSubAccounts(account1, ImmutableSet.of("subAccount-1", "subAccount-2"));
        AccountUtils.setSubAccounts(account2, ImmutableSet.of("subAccount-3", "subAccount-4"));
        assertThat(AccountUtils.getBankIdBySubAccount(Collections.singletonList(account1), "subAccount-1"))
                .isEqualTo("bankId-1");
        assertThat(AccountUtils.getBankIdBySubAccount(Collections.singletonList(account1), "subAccount-2"))
                .isEqualTo("bankId-1");
        assertThat(AccountUtils.getBankIdBySubAccount(Collections.singletonList(account2), "subAccount-3"))
                .isEqualTo("bankId-2");
        assertThat(AccountUtils.getBankIdBySubAccount(Collections.singletonList(account2), "subAccount-4"))
                .isEqualTo("bankId-2");
    }

    @Test
    public void ensureCorrectBankId_whenGettingBankIdBySubAccount_forMultipleAccounts_withSameSubAccount() {
        Account account1 = createAccount("bankId-1", false, false);
        Account account2 = createAccount("bankId-2", true, true);

        AccountUtils.setSubAccounts(account1, Collections.singleton("subAccount-1"));
        AccountUtils.setSubAccounts(account2, Collections.singleton("subAccount-1"));

        assertThat(AccountUtils.getBankIdBySubAccount(ImmutableList.of(account1, account2), "subAccount-1"))
                .isEqualTo("bankId-1");
        assertThat(AccountUtils.getBankIdBySubAccount(ImmutableList.of(account1, account2), "subAccount-1"))
                .isNotEqualTo("bankId-2");
    }

    private Account createAccount(String bankId, boolean isClosed, boolean isExcluded) {
        Account account = new Account();
        account.setBankId(bankId);
        account.setClosed(isClosed);
        account.setExcluded(isExcluded);

        return account;
    }
}
