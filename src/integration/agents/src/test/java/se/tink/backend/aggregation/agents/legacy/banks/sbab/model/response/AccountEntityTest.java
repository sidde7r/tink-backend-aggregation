package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;

public class AccountEntityTest {

    @Test
    public void entityBalanceWithDecimals_IsConvertedCorrectly() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setBalance("35 950,44 kr");
        Assert.assertEquals(accountEntity.toTinkAccount().get().getBalance(), 35950.44, 0);
    }

    @Test
    public void accountWithEmptyBalance_CannotBeConvertedToTinkAccount() {
        AccountEntity accountEntity = new AccountEntity();

        Assert.assertFalse(accountEntity.toTinkAccount().isPresent());

        accountEntity.setBalance("");
        Assert.assertFalse(accountEntity.toTinkAccount().isPresent());

        accountEntity.setBalance(" ");
        Assert.assertFalse(accountEntity.toTinkAccount().isPresent());

        accountEntity.setBalance("       ");
        Assert.assertFalse(accountEntity.toTinkAccount().isPresent());
    }

    @Test
    public void entityWithNewLineInName_ConvertsToTinkAccountWithoutNewLine() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setBalance("10 000,00 kr");
        accountEntity.setName("This is a name with a new line\n");
        Optional<Account> tinkAccount = accountEntity.toTinkAccount();

        Assert.assertEquals("This is a name with a new line", tinkAccount.get().getName());
    }

    @Test
    public void
            entityWithNewLineInAccountNumber_AndNoName_ConvertsToTinkAccountWithoutNewLineInName() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setBalance("10 000,00 kr");
        accountEntity.setAccountNumber("\n0123456789");
        Optional<Account> tinkAccount = accountEntity.toTinkAccount();

        Assert.assertEquals("0123456789", tinkAccount.get().getName());
    }

    @Test
    public void entityWithWindowsNewLineInName_ConvertsToTinkAccountWithoutNewLine() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setBalance("10 000,00 kr");
        accountEntity.setName("This is a name with a Windows new line\r\n");
        Optional<Account> tinkAccount = accountEntity.toTinkAccount();

        Assert.assertEquals("This is a name with a Windows new line", tinkAccount.get().getName());
    }
}
