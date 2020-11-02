package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class DnbAccountMapperTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/resources";
    private static final String TEST_BBAN = "test_bban";
    private static final String TEST_NAME = "test_name";
    private static final String TEST_CURRENCY = "test_currency";

    private DnbAccountMapper accountMapper;

    @Before
    public void setup() {
        accountMapper = new DnbAccountMapper();
    }

    @Test
    public void shouldReturnEmptyOptionalWhenFailedToParse() {
        // when
        Optional<TransactionalAccount> maybeAccount = accountMapper.toTinkAccount(null, null);

        // then
        assertThat(maybeAccount.isPresent()).isFalse();
    }

    @Test
    @Parameters(method = "accountNamesToExpectedTypes")
    public void shouldReturnSavingAccountForSomeAccountNames(
            String accountName, AccountTypes expectedType) {
        // given
        AccountEntity accountEntity = getTestAccount(accountName);
        BalancesResponse balancesResponse = getTestBalances();

        // when
        Optional<TransactionalAccount> maybeAccount =
                accountMapper.toTinkAccount(accountEntity, balancesResponse);

        // then
        assertThat(maybeAccount.isPresent()).isTrue();

        TransactionalAccount account = maybeAccount.get();
        assertThat(account.getType()).isEqualTo(expectedType);
    }

    @SuppressWarnings("unused")
    private static Object accountNamesToExpectedTypes() {
        return new Object[] {
            new Object[] {"SPAREKONTO", AccountTypes.SAVINGS},
            new Object[] {"SAVING", AccountTypes.SAVINGS},
            new Object[] {"SUPERSPAR", AccountTypes.SAVINGS},
            new Object[] {"PLASSERINGSKONTO", AccountTypes.SAVINGS},
            new Object[] {"PLASSERINGSkonto 123", AccountTypes.SAVINGS},
            new Object[] {"Anything else", AccountTypes.CHECKING},
        };
    }

    @Test
    public void shouldMapAccountProperly() {
        // given
        AccountEntity accountEntity = getTestAccount();
        BalancesResponse balancesResponse = getTestBalances();

        // when
        Optional<TransactionalAccount> maybeAccount =
                accountMapper.toTinkAccount(accountEntity, balancesResponse);

        // then
        assertThat(maybeAccount.isPresent()).isTrue();

        TransactionalAccount account = maybeAccount.get();
        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.isUniqueIdentifierEqual(TEST_BBAN)).isTrue();
        assertThat(account.getAccountNumber()).isEqualTo(TEST_BBAN);
        assertThat(account.getName()).isEqualTo(TEST_NAME);
        assertThat(account.getIdentifiers()).hasSize(1);
        assertThat(account.getIdentifiers().get(0))
                .isEqualTo(AccountIdentifier.create(AccountIdentifier.Type.NO, TEST_BBAN));
        assertThat(account.getApiIdentifier()).isEqualTo(TEST_BBAN);
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(1234.0, "NOK"));
    }

    private AccountEntity getTestAccount() {
        return getTestAccount(TEST_NAME);
    }

    private AccountEntity getTestAccount(String name) {
        return new AccountEntity(TEST_BBAN, name, TEST_CURRENCY);
    }

    private BalancesResponse getTestBalances() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "balances.json").toFile(), BalancesResponse.class);
    }
}
