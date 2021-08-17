package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountEntityTest {
    private static final String RESOURCES_BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/opbank/resources/";

    @Test
    public void testAccountWithOneOwner() {
        AccountEntity entity = accountEntityFromJson("transactional_account_one_owner.json");
        Optional<TransactionalAccount> maybeAccount = entity.toTinkAccount();
        assertThat(maybeAccount).isPresent();
        TransactionalAccount account = maybeAccount.get();

        assertThat(account.getParties()).containsExactly(new Party("JOHN DOE", Role.HOLDER));

        AccountIdentifier iban = new IbanIdentifier("OKOYFIHH", "FI1410093000123458");
        AccountIdentifier bban = new BbanIdentifier("10093000123458");
        assertThat(account.getIdentifiers()).containsExactlyInAnyOrder(iban, bban);
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.of("1.79", "EUR"));
    }

    @Test
    public void testAccountWithTwoOwners() {
        AccountEntity entity = accountEntityFromJson("transactional_account_two_owners.json");
        Optional<TransactionalAccount> maybeAccount = entity.toTinkAccount();
        assertThat(maybeAccount).isPresent();
        TransactionalAccount account = maybeAccount.get();

        assertThat(account.getParties())
                .containsExactlyInAnyOrder(
                        new Party("JOHN DOE", Role.HOLDER), new Party("JANE DOE", Role.HOLDER));
    }

    private AccountEntity accountEntityFromJson(String filename) {
        return SerializationUtils.deserializeFromString(
                new File(RESOURCES_BASE_PATH + filename), AccountEntity.class);
    }
}
