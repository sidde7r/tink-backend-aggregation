package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountEntityTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/dkb/resources/";

    @Test
    public void toTinkAccount_should_map_fields_properly() {

        // given
        AccountEntity accountEntity =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "account_entity.json").toFile(),
                        AccountEntity.class);
        BalanceEntity balance =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "balance_entity.json").toFile(),
                        BalanceEntity.class);
        accountEntity.setBalances(Arrays.asList(balance));

        // when
        Optional<TransactionalAccount> maybeTransactionalAccount = accountEntity.toTinkAccount();

        // then
        assertThat(maybeTransactionalAccount.get()).isNotNull();
        TransactionalAccount account = maybeTransactionalAccount.get();
        assertThat(account.getName()).isEqualTo("Sichteinlagen");
        assertThat(account.getIdentifiers()).hasSize(2);
        assertThat(account.getIdentifiers())
                .containsExactlyInAnyOrder(
                        new BbanIdentifier("500105177748448174"),
                        new IbanIdentifier("BYLADEM1001", "DE97500105177748448174"));
        assertThat(account.getAccountNumber()).isEqualTo("DE97500105177748448174");
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(62.84, "EUR"));
        assertThat(account.getParties()).hasSize(3);
        assertThat(account.getParties())
                .containsExactlyInAnyOrder(
                        new Party("Edmund Ordnung", Role.HOLDER),
                        new Party("Wanda Coniechcialaniemca", Role.HOLDER),
                        new Party("Hans Kloss Undklaus", Role.HOLDER));
    }
}
