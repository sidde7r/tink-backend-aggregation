package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Paths;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LuminorAccountFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/luminor/fetcher/accounts/resources";

    @Test
    public void shouldMapToTinkAccount() {

        // given
        AccountsResponse accountsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "accounts_response.json").toFile(),
                        AccountsResponse.class);
        // when
        TransactionalAccount result =
                accountsResponse
                        .getAccounts()
                        .get(0)
                        .toTinkAccount(java.util.Optional.of("John Doe"))
                        .orElse(null);

        // then
        assertThat(result).isEqualToComparingFieldByFieldRecursively(getExpectedAccountsResponse());
    }

    private TransactionalAccount getExpectedAccountsResponse() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(LuminorConstants.ACCOUNT_TYPE_MAPPER, "Payment card account")
                .withBalance(
                        BalanceModule.of(
                                new ExactCurrencyAmount(BigDecimal.valueOf(152.45), "EUR")))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("LT594010049500047594")
                                .withAccountNumber(
                                        "A01A77CCB3F8EE9F4B56E0C26EAAB19AD36A9741F1560DF57B71B0C287794542")
                                .withAccountName("My main account")
                                .addIdentifier(new IbanIdentifier("LT594010049500047594"))
                                .build())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, "LT594010049500047594")
                .setApiIdentifier(
                        "A01A77CCB3F8EE9F4B56E0C26EAAB19AD36A9741F1560DF57B71B0C287794542")
                .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .addHolderName("John Doe")
                .build()
                .orElseThrow(IllegalStateException::new);
    }
}
