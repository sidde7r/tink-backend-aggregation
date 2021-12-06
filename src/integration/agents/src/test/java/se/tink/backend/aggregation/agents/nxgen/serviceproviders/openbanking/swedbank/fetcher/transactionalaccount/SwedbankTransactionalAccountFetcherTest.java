package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.math.BigDecimal;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.MarketCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchOnlineTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankTransactionalAccountFetcherTest {

    private SwedbankApiClient spyClient;
    private PersistentStorage persistentStorage;
    private AgentComponentProvider componentProvider;
    private SwedbankTransactionalAccountFetcher swedbankTransactionalAccountFetcher;

    @Before
    public void setup() {
        componentProvider = mock(AgentComponentProvider.class);
        SwedbankApiClient apiClient;
        apiClient = mock(SwedbankApiClient.class);
        spyClient = spy(apiClient);
        persistentStorage = new PersistentStorage();
    }

    @Test
    public void shouldMapSeAccountCorrectly() {
        swedbankTransactionalAccountFetcher =
                new SwedbankTransactionalAccountFetcher(
                        spyClient, MarketCode.SE, persistentStorage, componentProvider);

        FetchAccountResponse response =
                SerializationUtils.deserializeFromString(
                        getSEAccounts(), FetchAccountResponse.class);

        AccountBalanceResponse balanceResponse =
                SerializationUtils.deserializeFromString(
                        getSEBalance(), AccountBalanceResponse.class);

        doReturn(response).when(spyClient).fetchAccounts();
        doReturn(balanceResponse)
                .when(spyClient)
                .getAccountBalance("bf6265426add55b6bed3d4f694bcad02");

        Collection<TransactionalAccount> result =
                swedbankTransactionalAccountFetcher.fetchAccounts();

        // Identifiers
        assertEquals(
                "bf6265426add55b6bed3d4f694bcad02",
                result.stream().findFirst().get().getApiIdentifier());
        assertEquals("815059231235734", result.stream().findFirst().get().getUniqueIdentifier());
        assertEquals("8150-5,923 123 573-4", result.stream().findFirst().get().getAccountNumber());
        assertTrue(
                result.stream()
                        .findFirst()
                        .get()
                        .getIdModule()
                        .getIdentifiers()
                        .contains(new IbanIdentifier("SE1230000815059436018123")));
        assertTrue(
                result.stream()
                        .findFirst()
                        .get()
                        .getIdModule()
                        .getIdentifiers()
                        .contains(new SwedishIdentifier("815059231235734")));
        assertEquals(AccountTypes.CHECKING, result.stream().findFirst().get().getType());
        assertEquals(
                AccountFlag.PSD2_PAYMENT_ACCOUNT,
                result.stream().findFirst().get().getAccountFlags().stream().findFirst().get());

        // Balances
        assertEquals(
                "SEK",
                result.stream().findFirst().get().getExactAvailableBalance().getCurrencyCode());
        assertEquals(
                BigDecimal.valueOf(5675.66),
                result.stream().findFirst().get().getExactAvailableBalance().getExactValue());
        assertEquals("SEK", result.stream().findFirst().get().getExactBalance().getCurrencyCode());
        assertEquals(
                BigDecimal.valueOf(5675.66),
                result.stream().findFirst().get().getExactBalance().getExactValue());
    }

    @Test
    public void shouldMapEEAccountCorrectly() {
        swedbankTransactionalAccountFetcher =
                new SwedbankTransactionalAccountFetcher(
                        spyClient, MarketCode.EE, persistentStorage, componentProvider);

        FetchAccountResponse response =
                SerializationUtils.deserializeFromString(
                        getEEAccounts(), FetchAccountResponse.class);

        FetchOnlineTransactionsResponse transactionsResponse =
                SerializationUtils.deserializeFromString(
                        getEETransactions(), FetchOnlineTransactionsResponse.class);

        AccountBalanceResponse balanceResponse =
                SerializationUtils.deserializeFromString(
                        getEEBalance(), AccountBalanceResponse.class);

        doReturn(response).when(spyClient).fetchAccounts();
        doReturn(balanceResponse)
                .when(spyClient)
                .getAccountBalance("20444455555333555558888888855555");

        // I have to mock this behaviour since it's void
        doReturn(transactionsResponse).when(spyClient).getOnlineTransactions(any(), any(), any());
        persistentStorage.put(
                StorageKeys.HOLDER_NAME, transactionsResponse.getAccount().getOwnerName());

        Collection<TransactionalAccount> result =
                swedbankTransactionalAccountFetcher.fetchAccounts();

        assertEquals(
                "Esbjorn Fakename", result.stream().findFirst().get().getHolderName().toString());
        // Identifiers
        assertEquals(
                "20444455555333555558888888855555",
                result.stream().findFirst().get().getApiIdentifier());
        assertEquals(
                "EE032311123456789015", result.stream().findFirst().get().getUniqueIdentifier());
        assertEquals("EE032311123456789015", result.stream().findFirst().get().getAccountNumber());
        assertTrue(
                result.stream()
                        .findFirst()
                        .get()
                        .getIdModule()
                        .getIdentifiers()
                        .contains(new IbanIdentifier("EE032311123456789015")));
        assertTrue(
                result.stream()
                        .findFirst()
                        .get()
                        .getIdModule()
                        .getIdentifiers()
                        .contains(new IbanIdentifier("HABAEE2X", "EE032311123456789015")));
        assertEquals(AccountTypes.CHECKING, result.stream().findFirst().get().getType());
        assertEquals(
                AccountFlag.PSD2_PAYMENT_ACCOUNT,
                result.stream().findFirst().get().getAccountFlags().stream().findFirst().get());

        // Balances
        assertEquals(
                "EUR",
                result.stream().findFirst().get().getExactAvailableBalance().getCurrencyCode());
        assertEquals(
                BigDecimal.valueOf(3676.89),
                result.stream().findFirst().get().getExactAvailableBalance().getExactValue());
        assertEquals("EUR", result.stream().findFirst().get().getExactBalance().getCurrencyCode());
        assertEquals(
                BigDecimal.valueOf(3665.99),
                result.stream().findFirst().get().getExactBalance().getExactValue());
    }

    @Test
    public void shouldGetSavedOwnerNameIfThereAreDifferentNamesForDifferentAccounts() {
        // Already saved name from other account
        persistentStorage.put(StorageKeys.HOLDER_NAME, "Absolutely Not Esbjorn Fakename");

        swedbankTransactionalAccountFetcher =
                new SwedbankTransactionalAccountFetcher(
                        spyClient, "EE", persistentStorage, componentProvider);

        FetchAccountResponse response =
                SerializationUtils.deserializeFromString(
                        getEEAccounts(), FetchAccountResponse.class);

        FetchOnlineTransactionsResponse transactionsResponse =
                SerializationUtils.deserializeFromString(
                        getEETransactions(), FetchOnlineTransactionsResponse.class);

        AccountBalanceResponse balanceResponse =
                SerializationUtils.deserializeFromString(
                        getEEBalance(), AccountBalanceResponse.class);

        doReturn(response).when(spyClient).fetchAccounts();
        doReturn(balanceResponse)
                .when(spyClient)
                .getAccountBalance("20444455555333555558888888855555");

        doReturn(transactionsResponse).when(spyClient).getOnlineTransactions(any(), any(), any());

        // The name that we get from response
        String ownerNameFromAccount = transactionsResponse.getAccount().getOwnerName();
        assertEquals("Esbjorn Fakename", ownerNameFromAccount);

        Collection<TransactionalAccount> result =
                swedbankTransactionalAccountFetcher.fetchAccounts();

        assertEquals(
                "Absolutely Not Esbjorn Fakename",
                result.stream().findFirst().get().getHolderName().toString());
    }

    private String getSEAccounts() {
        return "{\"accounts\":["
                + "{\"resourceId\":\"bf6265426add55b6bed3d4f694bcad02\","
                + "\"iban\":\"SE1230000815059436018123\","
                + "\"bban\":\"8150-5,923 123 573-4\","
                + "\"currency\":\"SEK\","
                + "\"product\":\"Privatkonto\","
                + "\"cashAccountType\":\"CACC\","
                + "\"name\":\"Name of account\","
                + "\"bankId\":\"08999\"}"
                + "]"
                + "}";
    }

    private String getSEBalance() {
        return "{\"account\":"
                + "{\"iban\":\"SE1230000815059436018123\"},"
                + "\"balances\":["
                + "{\"balanceType\":\"interimAvailable\","
                + "\"balanceAmount\":"
                + "{\"currency\":\"SEK\","
                + "\"amount\":\"5675.66\"}"
                + "},"
                + "{\"balanceType\":\"authorised\","
                + "\"balanceAmount\":"
                + "{\"currency\":\"SEK\","
                + "\"amount\":\"5675.66\"}"
                + "}"
                + "]}";
    }

    private String getEEAccounts() {
        return "{\"accounts\":["
                + "{\"resourceId\":\"20444455555333555558888888855555\","
                + "\"iban\":\"EE032311123456789015\","
                + "\"cashAccountType\":\"CACC\","
                + "\"currency\":\"EUR\","
                + "\"product\":\"CURRENT\","
                + "\"_links\":{"
                + "\"balances\":"
                + "{\"href\":\"/v3/accounts/20444455555333555558888888855555/balances\"},"
                + "\"transactions\":{"
                + "\"href\":\"/v3/accounts/20444455555333555558888888855555/transactions\"}"
                + "}"
                + "}]}";
    }

    private String getEEBalance() {
        return "{\"account\":"
                + "{\"iban\":\"EE032311123456789015\"},"
                + "\"balances\":["
                + "{\"balanceType\":\"authorised\","
                + "\"balanceAmount\":"
                + "{\"currency\":\"EUR\","
                + "\"amount\":\"3665.99\"},"
                + "\"referenceDate\":\"2021-07-13\"},"
                + "{\"balanceType\":\"interimAvailable\","
                + "\"balanceAmount\":"
                + "{\"currency\":\"EUR\","
                + "\"amount\":\"3676.89\"},"
                + "\"referenceDate\":\"2021-07-13\"}]}";
    }

    public String getEETransactions() {
        return "{\"account\":"
                + "{\"iban\":\"EE032311123456789015\","
                + "\"ownerName\":\"Esbjorn Fakename\"},"
                + "\"transactions\":"
                + "{\"booked\":["
                + "{\"transactionId\":\"2021042100264444-1\","
                + "\"creditorName\":\"thisIsACreditorName\","
                + "\"creditorAccount\":"
                + "{\"iban\":\"EE123345678912123456\"},"
                + "\"transactionAmount\":{"
                + "\"currency\":\"EUR\","
                + "\"amount\":\"-245.00\"},"
                + "\"bankTransactionCode\":\"PMNT\","
                + "\"bookingDate\":\"2021-04-21\","
                + "\"valueDate\":\"2021-04-21\","
                + "\"remittanceInformationUnstructured\":\"FakeStringOfCharacters\","
                + "\"remittanceInformationStructured\":\"ThisIsAlsoFake\"},"
                + "{\"transactionId\":\"2021060101874444-1\","
                + "\"transactionAmount\":"
                + "{\"currency\":\"EUR\","
                + "\"amount\":\"-8.66\"},"
                + "\"bankTransactionCode\":\"PMNT\","
                + "\"bookingDate\":\"2021-06-01\","
                + "\"valueDate\":\"2021-06-01\","
                + "\"remittanceInformationUnstructured\":\"ThisIsWhatFakeLooksLike\"},"
                + "{\"transactionId\":\"2021060600024444-1\","
                + "\"creditorName\":\"FakeCreditor\","
                + "\"creditorAccount\":"
                + "{\"iban\":\"EE123345678912123465\"},"
                + "\"transactionAmount\":{"
                + "\"currency\":\"EUR\","
                + "\"amount\":\"-500.00\"},"
                + "\"bankTransactionCode\":\"PMNT\","
                + "\"bookingDate\":\"2021-06-06\","
                + "\"valueDate\":\"2021-06-06\","
                + "\"remittanceInformationUnstructured\":\"Buying Milk - Hint this is fake\","
                + "\"remittanceInformationStructured\":\"000000000000\"}"
                + "]"
                + "}}";
    }
}
