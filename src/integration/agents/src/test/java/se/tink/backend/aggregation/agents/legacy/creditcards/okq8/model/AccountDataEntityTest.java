package se.tink.backend.aggregation.agents.creditcards.okq8.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;

public class AccountDataEntityTest {
    @Test
    public void testToTinkAccount() throws ParseException {
        AccountDataEntity accountDataEntity = new AccountDataEntity();
        accountDataEntity.setAccount("01231231234");
        accountDataEntity.setAvailable("12 345,67");
        accountDataEntity.setSaldo("-12 345,67");
        accountDataEntity.setLimit("123,45");
        accountDataEntity.setCardName("OKQ8 VISA");
        accountDataEntity.setOwnerName("Test Name");
        accountDataEntity.setOcr("0123123123499");

        Account account = accountDataEntity.toTinkAccount();

        assertThat(account.getBalance()).isEqualTo(12345.67);
        assertThat(account.getBankId()).isEqualTo("01231231234");
        assertThat(account.getName()).isEqualTo("OKQ8 Visa");
        assertThat(account.getType()).isEqualTo(AccountTypes.CREDIT_CARD);
        assertThat(account.getBalance()).isEqualTo(12345.67);
    }
}
