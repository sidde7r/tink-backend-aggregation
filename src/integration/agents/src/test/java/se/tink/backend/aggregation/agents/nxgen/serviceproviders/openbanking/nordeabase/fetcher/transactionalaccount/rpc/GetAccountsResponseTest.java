package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class GetAccountsResponseTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/nordeabase/resources";

    @Test
    public void shouldMapToTinkAccount() {
        // given
        GetAccountsResponse getAccountsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "accounts_response.json").toFile(),
                        GetAccountsResponse.class);

        // when
        List<TransactionalAccount> result =
                (List<TransactionalAccount>) getAccountsResponse.toTinkAccounts();

        // then
        assertThat(result.size()).isEqualTo(2);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i))
                    .isEqualToComparingFieldByFieldRecursively(getExpectedAccounts().get(i));
        }
    }

    private List<TransactionalAccount> getExpectedAccounts() {
        return Arrays.asList(
                TransactionalAccount.nxBuilder()
                        .withTypeAndFlagsFrom(
                                NordeaBaseConstants.ACCOUNT_TYPE_MAPPER,
                                "Current",
                                TransactionalAccountType.OTHER)
                        .withBalance(
                                BalanceModule.of(
                                        new ExactCurrencyAmount(BigDecimal.valueOf(1.12), "DKK")))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier("9593123646")
                                        .withAccountNumber("DK9150519593123646")
                                        .withAccountName("NAME LAST_NAME")
                                        .addIdentifier(new IbanIdentifier("DK9150519593123646"))
                                        .build())
                        .putInTemporaryStorage(
                                NordeaBaseConstants.StorageKeys.ACCOUNT_ID, "DK50519593123646-DKK")
                        .setApiIdentifier("DK50519593123646-DKK")
                        .build()
                        .orElseThrow(IllegalStateException::new),
                TransactionalAccount.nxBuilder()
                        .withTypeAndFlagsFrom(
                                NordeaBaseConstants.ACCOUNT_TYPE_MAPPER,
                                "Savings",
                                TransactionalAccountType.OTHER)
                        .withBalance(
                                BalanceModule.of(
                                        new ExactCurrencyAmount(BigDecimal.valueOf(0), "NOK")))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier("NO3750022184484")
                                        .withAccountNumber("NO3750022184484")
                                        .withAccountName("NOR_NAME NOR_LAST_NAME")
                                        .addIdentifier(new IbanIdentifier("NO3750022184484"))
                                        .build())
                        .putInTemporaryStorage(
                                NordeaBaseConstants.StorageKeys.ACCOUNT_ID, "NO50022184484-NOK")
                        .setApiIdentifier("NO50022184484-NOK")
                        .build()
                        .orElseThrow(IllegalStateException::new));
    }
}
