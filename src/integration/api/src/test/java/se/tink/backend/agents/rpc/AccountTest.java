package se.tink.backend.agents.rpc;

import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountTest {

    @Test
    public void ensureAccountWith_nullName_serializeWithFallback() throws Exception {

        Account account = new Account();
        account.setAccountNumber("12345678");
        account.setName(null);

        String serializedAccount = SerializationUtils.serializeToString(account);
        Account deserializedAccount = SerializationUtils.deserializeFromString(serializedAccount, Account.class);

        Assert.assertEquals(account.getAccountNumber(), deserializedAccount.getName());
    }

    @Test
    public void ensureAccountWith_nonNullName_serializeWithoutFallback() throws Exception {

        Account account = new Account();
        account.setAccountNumber("12345678");
        account.setName("AccountName");

        String serializedAccount = SerializationUtils.serializeToString(account);
        Account deserializedAccount = SerializationUtils.deserializeFromString(serializedAccount, Account.class);

        Assert.assertEquals("AccountName", deserializedAccount.getName());
    }
}
