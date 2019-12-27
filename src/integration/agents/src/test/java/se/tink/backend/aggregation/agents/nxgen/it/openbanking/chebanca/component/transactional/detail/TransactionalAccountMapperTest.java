package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.component.transactional.detail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.detail.TransactionalAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.AmountEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionalAccountMapperTest {

    private final String CHECKING_ACC_ID = "0001658072";
    private final String CHECKING_ACC_NUMBER = "IT04G0305801604100571657883";
    private final String CHECKING_NAME = "71657883";
    private final String CHECKING_AMOUNT = "814047.160";
    private final String CHECKING_CURRENCY = "EUR";

    private final String SAVINGS_ACC_ID = "0001658033";
    private final String SAVINGS_ACC_NUMBER = "IT04G0305801604100571656789";
    private final String SAVINGS_NAME = "7165700";
    private final String SAVINGS_AMOUNT = "7003.210";
    private final String SAVINGS_CURRENCY = "EUR";

    @Test
    public void testCheckingAccountMappedCorrectly() {
        // given
        AccountEntity accEntity = getCheckingAccountEntity();
        AmountEntity amountEntity = getCheckingAmountEntity();

        // when
        Optional<TransactionalAccount> accOptional =
                TransactionalAccountMapper.mapToTinkAccount(accEntity, amountEntity);

        // then
        assertTrue(accOptional.isPresent());
        TransactionalAccount account = accOptional.get();
        assertEquals(CHECKING_NAME, account.getName());
        assertEquals(CHECKING_ACC_NUMBER, account.getAccountNumber());
        assertEquals(CHECKING_ACC_ID, account.getApiIdentifier());
        assertEquals(AccountTypes.CHECKING, account.getType());
        assertEquals(
                ExactCurrencyAmount.of(CHECKING_AMOUNT, CHECKING_CURRENCY),
                account.getExactBalance());
    }

    @Test
    public void testSavingsAccountMappedCorrectly() {
        // given
        AccountEntity accEntity = getSavingsAccountEntity();
        AmountEntity amountEntity = getSavingsAmountEntity();

        // when
        Optional<TransactionalAccount> accOptional =
                TransactionalAccountMapper.mapToTinkAccount(accEntity, amountEntity);

        // then
        assertTrue(accOptional.isPresent());
        TransactionalAccount account = accOptional.get();
        assertEquals(SAVINGS_NAME, account.getName());
        assertEquals(SAVINGS_ACC_NUMBER, account.getAccountNumber());
        assertEquals(SAVINGS_ACC_ID, account.getApiIdentifier());
        assertEquals(AccountTypes.SAVINGS, account.getType());
        assertEquals(
                ExactCurrencyAmount.of(SAVINGS_AMOUNT, SAVINGS_CURRENCY),
                account.getExactBalance());
    }

    private AmountEntity getCheckingAmountEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"amount\": \""
                        + CHECKING_AMOUNT
                        + "\",\n"
                        + "  \"currency\": \""
                        + CHECKING_CURRENCY
                        + "\"\n"
                        + "}",
                AmountEntity.class);
    }

    private AccountEntity getCheckingAccountEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"accountId\": \""
                        + CHECKING_ACC_ID
                        + "\",\n"
                        + "  \"product\": {\n"
                        + "    \"code\": \"0633\",\n"
                        + "    \"description\": \"CONTO YELLOW\"\n"
                        + "  },\n"
                        + "  \"currency\": \"EUR\",\n"
                        + "  \"iban\": \""
                        + CHECKING_ACC_NUMBER
                        + "\",\n"
                        + "  \"name\": \""
                        + CHECKING_NAME
                        + "\"\n"
                        + "}",
                AccountEntity.class);
    }

    private AmountEntity getSavingsAmountEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"amount\": \""
                        + SAVINGS_AMOUNT
                        + "\",\n"
                        + "  \"currency\": \""
                        + SAVINGS_CURRENCY
                        + "\"\n"
                        + "    }",
                AmountEntity.class);
    }

    private AccountEntity getSavingsAccountEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"accountId\": \""
                        + SAVINGS_ACC_ID
                        + "\",\n"
                        + "  \"product\": {\n"
                        + "    \"code\": \"CDEP\",\n"
                        + "    \"description\": \"CONTO DEP\"\n"
                        + "  },\n"
                        + "  \"currency\": \"EUR\",\n"
                        + "  \"iban\": \""
                        + SAVINGS_ACC_NUMBER
                        + "\",\n"
                        + "  \"name\": \""
                        + SAVINGS_NAME
                        + "\"\n"
                        + "}",
                AccountEntity.class);
    }
}
