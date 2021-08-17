package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class UnicreditTransactionalAccountMapperTest {
    private final UnicreditTransactionalAccountMapper mapper =
            new UnicreditTransactionalAccountMapper();

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/unicredit/resources";

    @Test
    public void shouldTransformAccountCorrectly() {
        // given
        AccountEntity accountDetailsEntity =
                SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accountDetails.json").toFile(),
                                AccountDetailsResponse.class)
                        .getAccount();
        List<BalanceEntity> balances =
                SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "balances.json").toFile(),
                                BalancesResponse.class)
                        .getBalances();
        ;

        // when
        Optional<TransactionalAccount> result =
                mapper.toTinkAccount(accountDetailsEntity, balances);

        // then
        assertThat(result.get().getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(result.get().getIdentifiers())
                .contains(
                        new IbanIdentifier("DE90500105172649244436"),
                        new BbanIdentifier("500105172649244436"));
        assertThat(result.get().getApiIdentifier()).isEqualTo("test-resource-id");
        assertThat(result.get().getExactBalance())
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal("123.45"), "EUR"));
        assertThat(result.get().getAccountFlags()).contains(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        assertThat(result.get().getHolderName().toString())
                .isEqualTo("Test-Owner-Name-From-Details");
    }
}
