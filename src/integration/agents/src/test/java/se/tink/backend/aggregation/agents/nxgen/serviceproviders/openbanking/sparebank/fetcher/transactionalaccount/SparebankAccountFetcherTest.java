package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Iterator;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.util.AccountTestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.util.CustomAsserts;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class SparebankAccountFetcherTest {

    private static SparebankApiClient apiClient;

    @BeforeClass
    public static void setup() {
        apiClient = mock(SparebankApiClient.class);
    }

    @Test
    public void shouldParseJsonResponseToTransactionalAccountCorrectly() {
        SparebankAccountFetcher accountFetcher = new SparebankAccountFetcher(apiClient);
        when(apiClient.fetchAccounts()).thenReturn(AccountTestData.getAccountResponse());
        when(apiClient.fetchBalances(any(String.class)))
                .thenReturn(AccountTestData.getBalanceResponse());

        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        Iterator<TransactionalAccount> iterator = accounts.iterator();

        assertEquals(2, accounts.size());
        CustomAsserts.assertTransactionalAccountsEqual(
                iterator.next(),
                AccountTypes.CHECKING,
                ExactCurrencyAmount.of(100.26, "NOK"),
                "95360573002",
                "NO0995360573002",
                "Brukskonto",
                "NO0995360573002",
                "95360573002",
                "enc!!aI55HS7YpeXJhHqCXFPEGfF992bjRx2Gw==",
                "/v1/sandbox/accounts/asdf/transactions");

        CustomAsserts.assertTransactionalAccountsEqual(
                iterator.next(),
                AccountTypes.OTHER,
                ExactCurrencyAmount.of(100.26, "NOK"),
                "91351631959",
                "NO8591351631959",
                "",
                "NO8591351631959",
                "91351631959",
                "enc!!90hSjpSdpeftHiDSYiPaHVGoKHriD-6ARR4JlXcEebYbYa_WNwJvqevSrba4WtnD",
                "/v1/sandbox/accounts/zxcv/transactions");
    }

    private Object[] emptyAccountResponses() {
        return new Object[] {"{}", "{\"accounts\": null}", "{\"accounts\": []}"};
    }

    @Test
    @Parameters(method = "emptyAccountResponses")
    public void shouldReturnEmptyCollectionWhenBankReturnsNoAccounts(String emptyAccountResponse) {
        SparebankAccountFetcher accountFetcher = new SparebankAccountFetcher(apiClient);
        when(apiClient.fetchAccounts())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                emptyAccountResponse, AccountResponse.class));

        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        assertEquals(0, accounts.size());
    }
}
