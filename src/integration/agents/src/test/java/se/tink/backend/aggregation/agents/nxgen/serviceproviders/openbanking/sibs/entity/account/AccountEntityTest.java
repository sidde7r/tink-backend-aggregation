package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entity.account;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(JUnitParamsRunner.class)
public class AccountEntityTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        mapper.setVisibility(
                mapper.getSerializationConfig()
                        .getDefaultVisibilityChecker()
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    }

    private Object[] accountInJsonResponse() {
        return new Object[] {
            new Object[] {
                "{\"id\":\"dummy\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"bic\":\"dummy\"}",
                "EUR",
                "-6.27"
            },
            new Object[] {
                "{\"id\":\"dummy\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"bic\":\"dummy\"}",
                "EUR",
                "0.01"
            },
            new Object[] {
                "{\"id\":\"dummy\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"bic\":\"dummy\"}",
                "PLN",
                "-99999.99"
            },
        };
    }

    @Test
    @Parameters(method = "accountInJsonResponse")
    public void shouldCorrectlyConvertJsonAccountToTransactionalAccount(
            String json, String currency, String amount) throws IOException {
        AccountEntity accountEntity = mapper.readValue(json, AccountEntity.class);

        TransactionalAccount account =
                accountEntity
                        .toTinkAccount(new ExactCurrencyAmount(new BigDecimal(amount), currency))
                        .get();

        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.getExactBalance())
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal(amount), currency));
    }
}
