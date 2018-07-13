package se.tink.backend.aggregation.agents.banks.nordea.v15.model.savings;

import com.google.api.client.util.Preconditions;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CustodyAccountTest {

    @Test(expected = IllegalStateException.class)
    public void testAccountIdWithWrongNrOfDigits() {
        CustodyAccount account = createAccount("TEST:11");
        Preconditions.checkState(account.hasValidBankId());
    }

    @Test(expected = IllegalStateException.class)
    public void testAccountIdWithWrongStructure() {
        CustodyAccount account = createAccount("11111");
        Preconditions.checkState(account.hasValidBankId());
    }

    @Test
    public void testCorrectAccountIds() {
        assertThat(createAccount("TEST:11111111111111").hasValidBankId()).isTrue();
        assertThat(createAccount("TEST:11111111111").hasValidBankId()).isTrue();
        assertThat(createAccount("ASDF:1234567890123456789").hasValidBankId()).isTrue();
        assertThat(createAccount("ASDF:123456789012").hasValidBankId()).isTrue();
        assertThat(createAccount("ASDF:123456.1").hasValidBankId()).isTrue();
        assertThat(createAccount("ASDF:1234567890.1").hasValidBankId()).isTrue();
    }

    private CustodyAccount createAccount(String accountId) {
        CustodyAccount account = new CustodyAccount();
        account.setAccountId(accountId);
        return account;
    }

}