package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Iterator;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.data.AccountTestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.NorwegianIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class SparebankAccountFetcherTest {

    @Rule public ExpectedException thrown = ExpectedException.none();

    private static SparebankApiClient apiClient;

    @BeforeClass
    public static void setup() {
        apiClient = mock(SparebankApiClient.class);
    }

    @Test
    public void shouldParseJsonResponseToTransactionalAccountCorrectly() {
        SparebankAccountFetcher accountFetcher = new SparebankAccountFetcher(apiClient);
        when(apiClient.fetchAccounts()).thenReturn(AccountTestData.getAccountResponse());

        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        Iterator<TransactionalAccount> iterator = accounts.iterator();
        assertTransactionalAccountsEqual(
                iterator.next(),
                AccountTypes.CHECKING,
                ExactCurrencyAmount.of(100.26, "NOK"),
                "95360573002",
                "NO0995360573002",
                "Brukskonto",
                "NO0995360573002",
                "95360573002",
                "enc!!aI55HS7YpeXJhHqCXFPEGfF992bjRx2Gw==",
                "Brukskonto",
                "/v1/sandbox/accounts/asdf/transactions");

        assertTransactionalAccountsEqual(
                iterator.next(),
                AccountTypes.OTHER,
                ExactCurrencyAmount.of(10000.0, "NOK"),
                "91351631959",
                "NO8591351631959",
                "",
                "NO8591351631959",
                "91351631959",
                "enc!!90hSjpSdpeftHiDSYiPaHVGoKHriD-6ARR4JlXcEebYbYa_WNwJvqevSrba4WtnD",
                "",
                "/v1/sandbox/accounts/zxcv/transactions");
    }

    private void assertTransactionalAccountsEqual(
            TransactionalAccount account,
            AccountTypes accountType,
            ExactCurrencyAmount balance,
            String uniqueIdentifier,
            String accountNumber,
            String accountName,
            String iban,
            String bban,
            String apiIdentifier,
            String holderName,
            String transactionLink) {
        assertEquals(accountType, account.getType());
        assertEquals(balance, account.getExactBalance());
        assertTrue(
                "Unique identifier does not match!",
                account.isUniqueIdentifierEqual(uniqueIdentifier));
        assertEquals(accountNumber, account.getAccountNumber());
        assertEquals(accountName, account.getName());
        assertThat(
                account.getIdentifiers(),
                hasItems(new IbanIdentifier(iban), new NorwegianIdentifier(bban)));
        assertEquals(apiIdentifier, account.getApiIdentifier());
        assertEquals(holderName, account.getHolderName().toString());
        assertEquals(
                transactionLink,
                account.getFromTemporaryStorage(SparebankConstants.StorageKeys.TRANSACTIONS_URL));
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

    @Test
    public void shouldThrowIfNoBalancesInAccountResponse() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("No amount found");
        SparebankAccountFetcher accountFetcher = new SparebankAccountFetcher(apiClient);
        when(apiClient.fetchAccounts())
                .thenReturn(AccountTestData.getAccountResponseWithNoBalance());

        accountFetcher.fetchAccounts();
    }
}
