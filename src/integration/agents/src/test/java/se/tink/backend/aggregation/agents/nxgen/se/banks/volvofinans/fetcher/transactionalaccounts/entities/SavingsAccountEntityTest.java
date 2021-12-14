package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SavingsAccountEntityTest {

    @Test
    public void shouldMapToTinkSavingsAccount() {
        // given
        SavingsAccountEntity savingsAccountEntity = getSavingsAccountEntity();

        // when
        Optional<TransactionalAccount> optionalSavingsAccount =
                savingsAccountEntity.toTinkAccount();

        // then
        assertTrue(optionalSavingsAccount.isPresent());
        TransactionalAccount savingsAccount = optionalSavingsAccount.get();

        assertThat(savingsAccount.getIdModule().getUniqueId()).isEqualTo("7654321");
        assertThat(savingsAccount.getIdModule().getAccountNumber()).isEqualTo("7654321");
        assertThat(savingsAccount.getIdModule().getAccountName()).isEqualTo("Sparkonto");
        assertThat(savingsAccount.getApiIdentifier()).isEqualTo("savingAccountId123");
        assertThat(savingsAccount.getType()).isEqualTo(AccountTypes.SAVINGS);

        assertThat(savingsAccount.getExactBalance().getExactValue())
                .isEqualByComparingTo(BigDecimal.valueOf(2437.14));
        assertThat(savingsAccount.getExactBalance().getCurrencyCode()).isEqualTo("SEK");

        assertThat(savingsAccount.getIdentifiersAsList().size()).isEqualTo(1);
        AccountIdentifier accountIdentifier = savingsAccount.getIdentifiersAsList().get(0);
        assertThat(accountIdentifier.getIdentifier()).isEqualTo("7654321");

        assertThat(savingsAccount.getParties().size()).isEqualTo(1);
        Party party = savingsAccount.getParties().get(0);
        assertThat(party.getName()).isEqualTo("Firstname Lastname");
        assertThat(party.getRole()).isEqualTo(Role.HOLDER);
    }

    private SavingsAccountEntity getSavingsAccountEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"kontoId\": \"savingAccountId123\",\n"
                        + "  \"kontonummer\": \"7654321\",\n"
                        + "  \"produkt\": \"SPAR\",\n"
                        + "  \"namn\": \"Sparkonto\",\n"
                        + "  \"saldo\": 2437.14,\n"
                        + "  \"upplupenRanta\": 17.98,\n"
                        + "  \"rantesats\": 0.55,\n"
                        + "  \"kanOverfora\": true,\n"
                        + "  \"bankgiroInbetalning\": \"5347-9499\",\n"
                        + "  \"ocrnummerInbetalning\": \"7654321200\",\n"
                        + "  \"kontoRoll\": \"HUVUDSOKANDE\",\n"
                        + "  \"intressenter\": [\n"
                        + "    {\n"
                        + "      \"identitet\": \"199901011234\",\n"
                        + "      \"namn\": \"FirstName LastName\",\n"
                        + "      \"roll\": \"HUVUDSOKANDE\"\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}",
                SavingsAccountEntity.class);
    }
}
