package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.banks.sbab.entities.AccountEntity;

public class AccountEntityTest {

    @Test
    public void entityBalanceWithDecimals_IsConvertedCorrectly() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setBalance("35950.44");
        accountEntity.setAvailableBalance("0");
        Assert.assertEquals(accountEntity.toTinkAccount().get().getBalance(), 35950.44, 0);
    }

    @Test
    public void accountWithEmptyBalance_CannotBeConvertedToTinkAccount() {
        AccountEntity accountEntity = new AccountEntity();
        Throwable t = catchThrowable(accountEntity::toTinkAccount);
        Assert.assertTrue(t instanceof NullPointerException);

        accountEntity.setBalance("");
        t = catchThrowable(accountEntity::toTinkAccount);
        Assert.assertTrue(t instanceof NumberFormatException);

        accountEntity.setBalance(" ");
        t = catchThrowable(accountEntity::toTinkAccount);
        Assert.assertTrue(t instanceof NumberFormatException);

        accountEntity.setBalance("       ");
        t = catchThrowable(accountEntity::toTinkAccount);
        Assert.assertTrue(t instanceof NumberFormatException);
    }

    @Test
    public void entityWithNewLineInName_ConvertsToTinkAccountWithoutNewLine() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setBalance("1000.00");
        accountEntity.setAvailableBalance("0");
        accountEntity.setAccountaName("This is a name with a new line\n");
        Optional<Account> tinkAccount = accountEntity.toTinkAccount();

        Assert.assertEquals("This is a name with a new line", tinkAccount.get().getName());
    }

    @Test
    public void
            entityWithNewLineInAccountNumber_AndNoName_ConvertsToTinkAccountWithoutNewLineInName() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setBalance("10000.00");
        accountEntity.setAvailableBalance("0");
        accountEntity.setAccountNumber("\n0123456789");
        Optional<Account> tinkAccount = accountEntity.toTinkAccount();

        Assert.assertEquals("0123456789", tinkAccount.get().getName());
    }

    @Test
    public void entityWithWindowsNewLineInName_ConvertsToTinkAccountWithoutNewLine() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setBalance("100000.00");
        accountEntity.setAvailableBalance("0");
        accountEntity.setAccountaName("This is a name with a Windows new line\r\n");
        Optional<Account> tinkAccount = accountEntity.toTinkAccount();

        Assert.assertEquals("This is a name with a Windows new line", tinkAccount.get().getName());
    }
}
