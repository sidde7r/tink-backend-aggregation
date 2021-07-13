package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstans;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CitadeleTransactionalAccountFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/citadele/fetcher/transactionalaccount/resources";

    @Test
    public void shouldMapToTinkAccount() {

        // given
        ListAccountsResponse listAccountsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "accounts_response.json").toFile(),
                        ListAccountsResponse.class);
        // when
        TransactionalAccount result =
                listAccountsResponse.getAccounts().get(0).toTinkAccount().orElse(null);

        // then
        assertThat(result).isEqualToComparingOnlyGivenFields(getExpectedAccountsResponse().get(0));
    }

    private List<TransactionalAccount> getExpectedAccountsResponse() {
        return Collections.singletonList(
                TransactionalAccount.nxBuilder()
                        .withTypeAndFlagsFrom(CitadeleBaseConstans.ACCOUNT_TYPE_MAPPER, "PRIV")
                        .withBalance(
                                BalanceModule.of(
                                        new ExactCurrencyAmount(
                                                BigDecimal.valueOf(5877.78), "EUR")))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier("FR7612345987650123456789014")
                                        .withAccountNumber("FR7612345987650123456789014")
                                        .withAccountName("string")
                                        .addIdentifier(
                                                new IbanIdentifier("FR7612345987650123456789014"))
                                        .build())
                        .setApiIdentifier("string")
                        .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                        .addHolderName("John Doe")
                        .build()
                        .orElseThrow(IllegalStateException::new));
    }
}
