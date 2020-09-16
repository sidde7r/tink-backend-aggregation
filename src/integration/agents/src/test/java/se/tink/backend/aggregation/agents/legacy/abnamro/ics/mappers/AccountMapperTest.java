package se.tink.backend.aggregation.agents.abnamro.ics.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.abnamro.client.model.creditcards.CreditCardAccountEntity;

@RunWith(JUnitParamsRunner.class)
public class AccountMapperTest {

    private Logger log = LoggerFactory.getLogger(AccountMapperTest.class);

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
        assertThat(AccountMapper.toAccount(entity, log)).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNumberTooShort() {
        CreditCardAccountEntity entity = new CreditCardAccountEntity();
        entity.setCurrentBalance(100D);
        entity.setAuthorizedBalance(10D);

        entity.setContractNumber("1111111111");
        assertThat(AccountMapper.toAccount(entity, log)).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNumberTooLong() {
        CreditCardAccountEntity entity = new CreditCardAccountEntity();
        entity.setCurrentBalance(100D);
        entity.setAuthorizedBalance(10D);

        entity.setContractNumber("11111111111111111");
        assertThat(AccountMapper.toAccount(entity, log)).isNotNull();
    }

    @Test
    public void testAccountBalance() {
        CreditCardAccountEntity entity = new CreditCardAccountEntity();
        entity.setCurrentBalance(13D);
        entity.setAuthorizedBalance(.37D);
        entity.setContractNumber("1234567890123456");

        assertThat(AccountMapper.toAccount(entity, log).getBalance()).isEqualTo(-13.37D);
    }
}
