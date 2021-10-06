package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.detail;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(accOptional).isPresent();
        TransactionalAccount account = accOptional.get();
        assertThat(account.getName()).isEqualTo(ACC_NAME);
        assertThat(account.getAccountNumber()).isEqualTo(ACC_NUMBER);
        assertThat(account.getApiIdentifier()).isEqualTo(ACC_ID);
        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(BALANCE, CURRENCY));
        assertThat(account.getExactAvailableBalance())
                .isEqualTo(ExactCurrencyAmount.of(AVAILABLE_BALANCE, CURRENCY));
        assertThat(account.getExactCreditLimit())
                .isEqualTo(ExactCurrencyAmount.of(CREDIT_LIMIT, CURRENCY));
    }

    @Test
    public void shouldMapCheckingAccountCorrectlyWithoutCreditLimit() {
        // given
        AccountEntity accEntity =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "checking_account.json").toFile(),
                        AccountEntity.class);
        BalancesDataEntity amountEntity =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "balances_without_credit_limit.json").toFile(),
                        BalancesDataEntity.class);

        // when
        Optional<TransactionalAccount> accOptional =
                TransactionalAccountMapper.mapToTinkAccount(accEntity, amountEntity);

        // then
        assertThat(accOptional).isPresent();
        TransactionalAccount account = accOptional.get();
        assertThat(account.getName()).isEqualTo(ACC_NAME);
        assertThat(account.getAccountNumber()).isEqualTo(ACC_NUMBER);
        assertThat(account.getApiIdentifier()).isEqualTo(ACC_ID);
        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(BALANCE, CURRENCY));
        assertThat(account.getExactAvailableBalance())
                .isEqualTo(ExactCurrencyAmount.of(AVAILABLE_BALANCE, CURRENCY));
        assertThat(account.getExactCreditLimit()).isNull();
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
        assertThat(accOptional).isPresent();
        TransactionalAccount account = accOptional.get();
        assertThat(account.getName()).isEqualTo(ACC_NAME);
        assertThat(account.getAccountNumber()).isEqualTo(ACC_NUMBER);
        assertThat(account.getApiIdentifier()).isEqualTo(ACC_ID);
        assertThat(account.getType()).isEqualTo(AccountTypes.SAVINGS);
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(BALANCE, CURRENCY));
        assertThat(account.getExactAvailableBalance())
                .isEqualTo(ExactCurrencyAmount.of(AVAILABLE_BALANCE, CURRENCY));
        assertThat(account.getExactCreditLimit())
                .isEqualTo(ExactCurrencyAmount.of(CREDIT_LIMIT, CURRENCY));
    }
}
