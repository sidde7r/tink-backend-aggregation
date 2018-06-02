package se.tink.backend.system.cli.cleanup;

import com.google.common.collect.Multimap;
import java.util.Arrays;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.core.Account;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnitParamsRunner.class)
public class DetectDuplicatedAccountsCommandTest {

    @Test
    @Parameters({
            "00001613422, 1613422",
            "9270-734 912 7, 92707349127"
    })
    public void cleanUpId(String id, String result) {
        assertEquals(result, new DetectDuplicatedAccountsCommand().cleanUpId(id));
    }

    @Test
    public void countDuplicatedAccounts() {
        List<Account> accounts = Arrays.asList(createAccount("credentialsId1", "8 551 957-7"),
                createAccount("credentialsId1", "0085519577"),
                createAccount("credentialsId1", "8551-9577"),
                createAccount("credentialsId1", "8 718 577-3"),
                createAccount("credentialsId1", "0087185773"),
                createAccount("credentialsId2", "0087185773"),
                createAccount("credentialsId2", "0087184567"));

        DetectDuplicatedAccountsCommand detectDuplicatedAccountsCommand = new DetectDuplicatedAccountsCommand();
        Multimap<String, Account> duplicatedAccounts = detectDuplicatedAccountsCommand
                .extractDuplicatedAccounts(accounts);

        int expKeysSize = 2;
        String expKey1 = detectDuplicatedAccountsCommand.accountKey(accounts.get(0));
        int expSizeForKey1 = 3;
        String expKey2 = detectDuplicatedAccountsCommand.accountKey(accounts.get(3));
        int expSizeForKEy2 = 2;

        assertEquals(expKeysSize, duplicatedAccounts.keySet().size());
        assertNotNull(duplicatedAccounts.get(expKey1));
        assertEquals(expSizeForKey1, duplicatedAccounts.get(expKey1).size());
        assertNotNull(duplicatedAccounts.get(expKey2));
        assertEquals(expSizeForKEy2, duplicatedAccounts.get(expKey2).size());
    }

    private Account createAccount(String credentialsId, String bankId) {
        Account account = new Account();
        account.setCredentialsId(credentialsId);
        account.setBankId(bankId);

        return account;
    }
}
