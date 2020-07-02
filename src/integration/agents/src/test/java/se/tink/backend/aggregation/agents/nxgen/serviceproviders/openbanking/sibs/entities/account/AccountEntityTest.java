package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.agents.rpc.AccountTypes.CHECKING;
import static se.tink.backend.agents.rpc.AccountTypes.CREDIT_CARD;
import static se.tink.backend.agents.rpc.AccountTypes.LOAN;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
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

    @SuppressWarnings("unused")
    private Object[] loansInJsonResponse() {
        return new Object[] {
            new Object[] {
                "{\"id\":\"dummy\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"bic\":\"dummy\"}",
                "EUR",
                "6.27"
            },
            new Object[] {
                "{\"id\":\"dummy\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"bic\":\"dummy\"}",
                "EUR",
                "0.01"
            },
            new Object[] {
                "{\"id\":\"dummy\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"bic\":\"dummy\"}",
                "PLN",
                "99999.99"
            },
        };
    }

    @SuppressWarnings("unused")
    private Object[] accountsInJsonResponse() {
        return new Object[] {
            new Object[] {
                "{\"id\":\"dummy\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"pan\":\"1234\"}",
                "EUR",
                "-6.27"
            },
            new Object[] {
                "{\"id\":\"dummy\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"pan\":\"1244243\"}",
                "EUR",
                "0.01"
            },
            new Object[] {
                "{\"id\":\"dummy\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"pan\":\"4234242324\"}",
                "PLN",
                "-99999.99"
            },
        };
    }

    @Test
    @Parameters(method = "accountsInJsonResponse")
    public void shouldCorrectlyConvertJsonAccountToTransactionalAccount(
            String json, String currency, String amount) throws IOException {
        AccountEntity accountEntity = mapper.readValue(json, AccountEntity.class);

        TransactionalAccount account =
                accountEntity
                        .toTinkAccount(new ExactCurrencyAmount(new BigDecimal(amount), currency))
                        .get();

        assertThat(account.getType()).isEqualTo(CHECKING);
        assertThat(account.getExactBalance())
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal(amount), currency));
    }

    @Test
    @Parameters(method = "loansInJsonResponse")
    public void shouldCorrectlyConvertJsonAccountToLoanAccount(
            String json, String currency, String amount) throws IOException {
        AccountEntity accountEntity = mapper.readValue(json, AccountEntity.class);

        LoanAccount account =
                accountEntity.toTinkLoan(
                        new ExactCurrencyAmount(new BigDecimal(amount).abs(), currency));

        assertThat(account.getType()).isEqualTo(LOAN);
        assertThat(account.getExactBalance())
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal(amount), currency));
    }

    @Test
    @Parameters(method = "accountsInJsonResponse")
    public void shouldCorrectlyConvertJsonAccountToCreditCardAccount(
            String json, String currency, String amount) throws IOException {
        AccountEntity accountEntity = mapper.readValue(json, AccountEntity.class);

        CreditCardAccount account =
                accountEntity.toTinkCreditCard(
                        new ExactCurrencyAmount(new BigDecimal(amount), currency));

        assertThat(account.getType()).isEqualTo(CREDIT_CARD);
        assertThat(account.getExactBalance())
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal(amount), currency));
    }
}
