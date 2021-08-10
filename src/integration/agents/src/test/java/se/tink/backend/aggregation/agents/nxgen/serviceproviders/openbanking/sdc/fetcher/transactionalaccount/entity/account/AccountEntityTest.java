package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.entity.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountEntityTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sdc/resources";

    @Test
    public void toTinkAccountWhenNameDoesNotContainSpareMapToChecking() {
        AccountEntity accountEntity =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "checking_account_entity.json").toFile(),
                        AccountEntity.class);

        // when
        Optional<TransactionalAccount> result =
                accountEntity.toTinkAccount(ExactCurrencyAmount.inEUR(123.45));

        // then
        assertThat(result).isPresent();
        assertThat(result.get())
                .isEqualToComparingFieldByFieldRecursively(
                        TransactionalAccount.nxBuilder()
                                .withType(TransactionalAccountType.CHECKING)
                                .withPaymentAccountFlag()
                                .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(123.45)))
                                .withId(
                                        IdModule.builder()
                                                .withUniqueIdentifier("DK09123456789")
                                                .withAccountNumber("123456789")
                                                .withAccountName("sample name")
                                                .addIdentifier(new IbanIdentifier("DK09123456789"))
                                                .addIdentifier(new BbanIdentifier("123456789"))
                                                .build())
                                .setApiIdentifier("sample-resource-id")
                                .setBankIdentifier("sample-resource-id")
                                .setHolderType(AccountHolderType.PERSONAL)
                                .addParties(new Party("", Party.Role.UNKNOWN))
                                .build()
                                .get());
    }

    @Test
    public void toTinkAccountWhenNameContainsSpareMapToSavings() {
        AccountEntity accountEntity =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "savings_type_account_entity.json").toFile(),
                        AccountEntity.class);

        // when
        Optional<TransactionalAccount> result =
                accountEntity.toTinkAccount(ExactCurrencyAmount.inEUR(123.45));

        // then
        assertThat(result).isPresent();
        assertThat(result.get())
                .isEqualToComparingFieldByFieldRecursively(
                        TransactionalAccount.nxBuilder()
                                .withType(TransactionalAccountType.SAVINGS)
                                .withPaymentAccountFlag()
                                .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(123.45)))
                                .withId(
                                        IdModule.builder()
                                                .withUniqueIdentifier("DK09123456789")
                                                .withAccountNumber("123456789")
                                                .withAccountName("sample spare name")
                                                .addIdentifier(new IbanIdentifier("DK09123456789"))
                                                .addIdentifier(new BbanIdentifier("123456789"))
                                                .build())
                                .setApiIdentifier("sample-resource-id")
                                .setBankIdentifier("sample-resource-id")
                                .setHolderType(AccountHolderType.PERSONAL)
                                .addParties(new Party("", Party.Role.UNKNOWN))
                                .build()
                                .get());
    }

    @Test
    public void toTinkAccountWhenNameContainsUppercaseSpareMapToSavings() {
        AccountEntity accountEntity =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "savings_type_account_entity2.json").toFile(),
                        AccountEntity.class);

        List<Party> partyList =
                Arrays.asList(
                        new Party("John X", Party.Role.UNKNOWN),
                        new Party("John Y", Party.Role.UNKNOWN));

        // when
        Optional<TransactionalAccount> result =
                accountEntity.toTinkAccount(ExactCurrencyAmount.inEUR(123.45));

        // then
        assertThat(result).isPresent();
        assertThat(result.get())
                .isEqualToComparingFieldByFieldRecursively(
                        TransactionalAccount.nxBuilder()
                                .withType(TransactionalAccountType.SAVINGS)
                                .withPaymentAccountFlag()
                                .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(123.45)))
                                .withId(
                                        IdModule.builder()
                                                .withUniqueIdentifier("DK09123456789")
                                                .withAccountNumber("123456789")
                                                .withAccountName("SAMPLE SPARE NAME")
                                                .addIdentifier(new IbanIdentifier("DK09123456789"))
                                                .addIdentifier(new BbanIdentifier("123456789"))
                                                .build())
                                .setApiIdentifier("sample-resource-id")
                                .setBankIdentifier("sample-resource-id")
                                .setHolderType(AccountHolderType.PERSONAL)
                                .addParties(partyList)
                                .build()
                                .get());
    }
}
