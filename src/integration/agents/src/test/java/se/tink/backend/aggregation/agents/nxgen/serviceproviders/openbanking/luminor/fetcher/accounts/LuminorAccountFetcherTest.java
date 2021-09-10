package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.LuminorAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LuminorAccountFetcherTest {

    LuminorApiClient client;
    LuminorAccountFetcher fetcher;
    LocalDateTimeSource localDateTimeSource;
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/luminor/fetcher/accounts/resources";

    @Before
    public void setup() {
        client = mock(LuminorApiClient.class);
        localDateTimeSource = new ConstantLocalDateTimeSource();
        fetcher = new LuminorAccountFetcher(client);
    }

    @Test
    public void shouldMapToTinkAccountWhenThereAreAccounts() {

        // given
        AccountsResponse accountsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "accounts_response.json").toFile(),
                        AccountsResponse.class);
        // when
        TransactionalAccount result =
                accountsResponse.getAccounts().get(0).toTinkAccount().orElse(null);

        // then
        assertThat(result).isEqualToComparingFieldByFieldRecursively(getExpectedAccountsResponse());
    }

    @Test
    public void shouldFetchAccounts() {
        AccountsResponse accountsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "accounts_response.json").toFile(),
                        AccountsResponse.class);
        when(client.getAccounts()).thenReturn(accountsResponse);

        Collection<TransactionalAccount> result = fetcher.fetchAccounts();
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyListIfThereAreNoAccounts() {
        AccountsResponse accountsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "no_accounts_response.json").toFile(),
                        AccountsResponse.class);
        when(client.getAccounts()).thenReturn(accountsResponse);

        Collection<TransactionalAccount> result = fetcher.fetchAccounts();
        Assert.assertTrue(result.isEmpty());
    }

    private TransactionalAccount getExpectedAccountsResponse() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(LuminorConstants.ACCOUNT_TYPE_MAPPER, "Payment card account")
                .withBalance(
                        BalanceModule.of(
                                new ExactCurrencyAmount(BigDecimal.valueOf(152.45), "EUR")))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("LT000000000000000094")
                                .withAccountNumber(
                                        "A01A77CCB3F8EE9F4B56E0C26THISISAFAKEACCOUNT60DF57B71B0C28779XXXX")
                                .withAccountName("My main account")
                                .addIdentifier(new IbanIdentifier("LT000000000000000094"))
                                .build())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, "LT000000000000000094")
                .setApiIdentifier(
                        "A01A77CCB3F8EE9F4B56E0C26THISISAFAKEACCOUNT60DF57B71B0C28779XXXX")
                .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .addHolderName("Esbjorn Fakename")
                .build()
                .orElseThrow(IllegalStateException::new);
    }
}
