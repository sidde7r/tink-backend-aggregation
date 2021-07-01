package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.fetcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.TestDataReader;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(JUnitParamsRunner.class)
public class ConsorsbankAccountMapperTest {

    private ConsorsbankAccountMapper accountMapper = new ConsorsbankAccountMapper();

    private FetchAccountsResponse testAccounts =
            TestDataReader.readFromFile(TestDataReader.TWO_ACCOUNTS, FetchAccountsResponse.class);

    @Test
    @Parameters(method = "typesAndExpected")
    public void shouldMapAccountTypeAsExpected(String type, TransactionalAccountType expected) {
        // given
        AccountEntity accountEntity = testAccounts.getAccounts().get(0);
        accountEntity.setCashAccountType(type);

        // when
        Optional<TransactionalAccount> maybeAccount = accountMapper.toTinkAccount(accountEntity);

        // then
        assertThat(maybeAccount.isPresent()).isTrue();
        TransactionalAccount account = maybeAccount.get();
        assertThat(account.getType()).isEqualTo(expected.toAccountType());
    }

    @Test
    @Parameters({"Unknown", "SSSS", "noClueWhat"})
    public void shouldFailToMapWhenUnknownType(String type) {
        // given
        AccountEntity accountEntity = testAccounts.getAccounts().get(0);
        accountEntity.setCashAccountType(type);

        // when
        Optional<TransactionalAccount> maybeAccount = accountMapper.toTinkAccount(accountEntity);

        // then
        assertThat(maybeAccount.isPresent()).isFalse();
    }

    @Test
    public void shouldMapAccountProperly() {
        // given
        AccountEntity accountEntity = testAccounts.getAccounts().get(1);

        // when
        Optional<TransactionalAccount> maybeAccount = accountMapper.toTinkAccount(accountEntity);

        // then
        assertThat(maybeAccount.isPresent()).isTrue();
        TransactionalAccount account = maybeAccount.get();
        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(14.61, "EUR"));
        assertThat(account.getHolderName().toString()).isEqualTo("Name Surname");
        assertThat(account.getApiIdentifier()).isEqualTo("asdf4321");
        assertThat(account.getAccountNumber()).isEqualTo("DE4321");
        assertThat(account.isUniqueIdentifierEqual("DE4321")).isTrue();
        assertThat(account.getName()).isEqualTo("DE4321");
        assertThat(account.getIdentifiers()).containsExactly(new IbanIdentifier("DE4321"));
    }

    private Object[] typesAndExpected() {

        return new Object[] {
            new Object[] {"CACC", TransactionalAccountType.CHECKING},
            new Object[] {"CASH", TransactionalAccountType.CHECKING},
            new Object[] {"LLSV", TransactionalAccountType.SAVINGS},
            new Object[] {"ONDP", TransactionalAccountType.SAVINGS},
            new Object[] {"SVGS", TransactionalAccountType.SAVINGS},
        };
    }
}
