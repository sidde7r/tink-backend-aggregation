package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.account;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountEntityTest {

    @Test
    public void toTinkAccount() {
        // given
        AccountEntity entity = accountAsJson(accountEntityProps());
        // and
        List<BalanceEntity> balances = Collections.singletonList(getExampleBalanceEntity());
        AccountEntity accountDetailsEntity = accountAsJson(accountDetailsEntityProps());

        // when
        Optional<TransactionalAccount> result =
                entity.toTinkAccount(accountDetailsEntity, balances);

        // then
        assertThat(result.get().getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(result.get().getIdentifiers()).contains(new IbanIdentifier("test-iban"));
        assertThat(result.get().getApiIdentifier()).isEqualTo("test-resource-id");
        assertThat(result.get().getExactBalance())
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal("123.45"), "EUR"));
        assertThat(result.get().getAccountFlags()).contains(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        assertThat(result.get().getHolderName().toString())
                .isEqualTo("Test-Owner-Name-From-Details");
    }

    @Test
    public void shouldTakeThingsFromDetailsEntityIfMissingInBase() {
        // given
        AccountEntity entity = accountAsJson(accountEntityMinimalProps());
        // and
        List<BalanceEntity> balances = Collections.singletonList(getExampleBalanceEntity());
        AccountEntity accountDetailsEntity = accountAsJson(accountDetailsEntityProps());

        // when
        Optional<TransactionalAccount> result =
                entity.toTinkAccount(accountDetailsEntity, balances);

        // then
        assertThat(result.get().getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(result.get().getIdentifiers())
                .contains(new IbanIdentifier("test-iban-from-details"));
        assertThat(result.get().getApiIdentifier()).isEqualTo("test-resource-id");
        assertThat(result.get().getExactBalance())
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal("123.45"), "EUR"));
        assertThat(result.get().getAccountFlags()).contains(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        assertThat(result.get().getHolderName().toString())
                .isEqualTo("Test-Owner-Name-From-Details");
    }

    private Properties accountEntityProps() {
        Properties account = new Properties();
        account.setProperty("resourceId", "test-resource-id");
        account.setProperty("iban", "test-iban");
        account.setProperty("currency", "test-currency");
        account.setProperty("cashAccountType", "test-cash-account-type");
        account.setProperty("name", "test-name");
        return account;
    }

    private Properties accountEntityMinimalProps() {
        Properties account = new Properties();
        account.setProperty("resourceId", "test-resource-id");
        return account;
    }

    private Properties accountDetailsEntityProps() {
        Properties account = new Properties();
        account.setProperty("resourceId", "test-resource-id-from-details");
        account.setProperty("iban", "test-iban-from-details");
        account.setProperty("currency", "test-currency-from-details");
        account.setProperty("cashAccountType", "test-cash-account-type-from-details");
        account.setProperty("name", "test-name-from-details");
        account.setProperty("ownerName", "test-owner-name-from-details");
        return account;
    }

    private static AccountEntity accountAsJson(final Properties account) {
        Gson gsonObj = new Gson();
        return SerializationUtils.deserializeFromString(
                gsonObj.toJson(account), AccountEntity.class);
    }

    private static BalanceEntity getExampleBalanceEntity() {
        return SerializationUtils.deserializeFromString(
                "{\"balanceAmount\": {\"amount\": \"123.45\", \"currency\": \"EUR\"}, \"balanceType\": \"expected\", \"creditLimitIncluded\": false, \"lastChangeDateTime\": \"2020-06-08T09:25:00+02:00\"}",
                BalanceEntity.class);
    }
}
