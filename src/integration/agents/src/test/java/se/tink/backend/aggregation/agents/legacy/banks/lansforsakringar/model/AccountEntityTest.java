package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;

public class AccountEntityTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ACCOUNT =
            "{\"accountName\": \"namnKonto\",\"accountNumber\": \"90212345678\",\"type\": \"UNKNOWN\",\"balance\": \"0.0\",\"bankName\": null,\"clearingNumber\": \"0\",\"localAccount\": false,\"savedRecipient\": false}";

    @Test
    public void shouldMapAccountTypeUnknownToCheckingAccount() throws IOException {
        AccountEntity accountEntity = MAPPER.readValue(ACCOUNT, AccountEntity.class);
        Account account = accountEntity.toAccount();
        accountEntity.setTypeForAccountTypeUnknown(account);

        Assert.assertEquals(AccountTypes.CHECKING, account.getType());
    }

    @Test
    public void shouldMapAccountTypeUnknownToSavingsAccount() throws IOException {
        AccountEntity accountEntity = MAPPER.readValue(ACCOUNT, AccountEntity.class);
        accountEntity.setAccountName("sparkonto");
        Account account = accountEntity.toAccount();
        accountEntity.setTypeForAccountTypeUnknown(account);

        Assert.assertEquals(AccountTypes.SAVINGS, account.getType());
    }

    @Test
    public void shouldMapAccountTypeUnknownToInvestmentAccount() throws IOException {
        AccountEntity accountEntity = MAPPER.readValue(ACCOUNT, AccountEntity.class);
        accountEntity.setAccountName("aktiekonto");
        Account account = accountEntity.toAccount();
        accountEntity.setTypeForAccountTypeUnknown(account);

        Assert.assertEquals(AccountTypes.INVESTMENT, account.getType());
    }
}
