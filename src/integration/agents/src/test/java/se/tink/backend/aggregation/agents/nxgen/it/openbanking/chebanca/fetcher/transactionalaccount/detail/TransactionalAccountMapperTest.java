package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.detail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.BalancesDataEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionalAccountMapperTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/chebanca/resources";
    private final String ACC_ID = "0001658072";
    private final String ACC_NUMBER = "IT04G0305801604100571657883";
    private final String ACC_NAME = "71657883";
    private final String BALANCE = "14719.000";
    private final String AVAILABLE_BALANCE = "15719.000";
    private final String CREDIT_LIMIT = "17719.000";
    private final String CURRENCY = "EUR";

    @Test
    public void shouldMapCheckingAccountCorrectly() {
        // given
        AccountEntity accEntity =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "checking_account.json").toFile(),
                        AccountEntity.class);
        BalancesDataEntity amountEntity =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "balances.json").toFile(),
                        BalancesDataEntity.class);

        // when
        Optional<TransactionalAccount> accOptional =
                TransactionalAccountMapper.mapToTinkAccount(accEntity, amountEntity);

        // then
        assertTrue(accOptional.isPresent());
        TransactionalAccount account = accOptional.get();
        assertEquals(ACC_NAME, account.getName());
        assertEquals(ACC_NUMBER, account.getAccountNumber());
        assertEquals(ACC_ID, account.getApiIdentifier());
        assertEquals(AccountTypes.CHECKING, account.getType());
        assertEquals(ExactCurrencyAmount.of(BALANCE, CURRENCY), account.getExactBalance());
        assertEquals(
                ExactCurrencyAmount.of(AVAILABLE_BALANCE, CURRENCY),
                account.getExactAvailableBalance());
        assertEquals(ExactCurrencyAmount.of(CREDIT_LIMIT, CURRENCY), account.getExactCreditLimit());
    }

    @Test
    public void shouldMapSavingsAccountCorrectly() {
        // given
        AccountEntity accEntity =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "saving_account.json").toFile(),
                        AccountEntity.class);
        BalancesDataEntity amountEntity =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "balances.json").toFile(),
                        BalancesDataEntity.class);

        // when
        Optional<TransactionalAccount> accOptional =
                TransactionalAccountMapper.mapToTinkAccount(accEntity, amountEntity);

        // then
        assertTrue(accOptional.isPresent());
        TransactionalAccount account = accOptional.get();
        assertEquals(ACC_NAME, account.getName());
        assertEquals(ACC_NUMBER, account.getAccountNumber());
        assertEquals(ACC_ID, account.getApiIdentifier());
        assertEquals(AccountTypes.SAVINGS, account.getType());
        assertEquals(ExactCurrencyAmount.of(BALANCE, CURRENCY), account.getExactBalance());
        assertEquals(
                ExactCurrencyAmount.of(AVAILABLE_BALANCE, CURRENCY),
                account.getExactAvailableBalance());
        assertEquals(ExactCurrencyAmount.of(CREDIT_LIMIT, CURRENCY), account.getExactCreditLimit());
    }
}
