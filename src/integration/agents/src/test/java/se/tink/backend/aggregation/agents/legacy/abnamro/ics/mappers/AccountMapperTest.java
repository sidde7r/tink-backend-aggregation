package se.tink.backend.aggregation.agents.abnamro.ics.mappers;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.libraries.abnamro.client.model.creditcards.CreditCardAccountEntity;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class AccountMapperTest {

    @Test
    @Parameters({
            "12345678901",
            "123456789012",
            "1234567890123",
            "12345678901234",
            "123456789012345",
            "1234567890123456"
    })
    public void testValidContractNumbers(String contractNumber) {
        CreditCardAccountEntity entity = new CreditCardAccountEntity();
        entity.setCurrentBalance(100D);
        entity.setAuthorizedBalance(10D);

        entity.setContractNumber(contractNumber);
        assertThat(AccountMapper.toAccount(entity)).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNumberTooShort() {
        CreditCardAccountEntity entity = new CreditCardAccountEntity();
        entity.setCurrentBalance(100D);
        entity.setAuthorizedBalance(10D);

        entity.setContractNumber("1111111111");
        assertThat(AccountMapper.toAccount(entity)).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNumberTooLong() {
        CreditCardAccountEntity entity = new CreditCardAccountEntity();
        entity.setCurrentBalance(100D);
        entity.setAuthorizedBalance(10D);

        entity.setContractNumber("11111111111111111");
        assertThat(AccountMapper.toAccount(entity)).isNotNull();
    }

    @Test
    public void testAccountBalance() {
        CreditCardAccountEntity entity = new CreditCardAccountEntity();
        entity.setCurrentBalance(13D);
        entity.setAuthorizedBalance(.37D);
        entity.setContractNumber("1234567890123456");

        assertThat(AccountMapper.toAccount(entity).getBalance()).isEqualTo(-13.37D);
    }
}
