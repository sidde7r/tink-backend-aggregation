package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.entities;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc.AuthorisationStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.BalanceResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountEntityTest {

    LhvApiClient spyClient;
    SessionStorage sessionStorage;

    @Before
    public void setup() {
        LhvApiClient apiClient = mock(LhvApiClient.class);
        spyClient = spy(apiClient);
        sessionStorage = new SessionStorage();
    }

    @Test
    public void shouldMapAccountCorrectly() {
        AuthorisationStatusResponse authorisationStatusResponse =
                SerializationUtils.deserializeFromString(
                        getProfiles(), AuthorisationStatusResponse.class);
        AccountResponse accountResponse =
                SerializationUtils.deserializeFromString(getAccount(), AccountResponse.class);
        BalanceResponse balanceResponse =
                SerializationUtils.deserializeFromString(getBalance(), BalanceResponse.class);

        sessionStorage.put(
                StorageKeys.AVAILABLE_ROLES, authorisationStatusResponse.getAvailableRoles());

        doReturn(balanceResponse).when(spyClient).fetchAccountBalance(any());

        TransactionalAccount result =
                accountResponse.getAccountList().stream()
                        .findFirst()
                        .get()
                        .toTinkAccount(spyClient, sessionStorage)
                        .orElse(null);

        // Holder
        Assert.assertEquals("Jane Doe", Objects.requireNonNull(result).getHolderName().toString());

        // Identifiers
        Assert.assertEquals(
                "EE382200221020145685", Objects.requireNonNull(result).getUniqueIdentifier());
        Assert.assertEquals(
                "EE382200221020145685", Objects.requireNonNull(result).getAccountNumber());
        Assert.assertEquals(
                "6103210e-d01e-4b53-adfc-01d53485a1ff",
                Objects.requireNonNull(result).getApiIdentifier());
        Assert.assertEquals(
                Optional.of("iban://LHVBEE22/EE382200221020145685").toString(),
                Objects.requireNonNull(result).getIdentifiers().stream().findFirst().toString());
        Assert.assertEquals(
                Optional.of(AccountFlag.PSD2_PAYMENT_ACCOUNT),
                Objects.requireNonNull(result).getAccountFlags().stream().findFirst());

        // Balances
        Assert.assertEquals(
                "EUR", Objects.requireNonNull(result).getExactAvailableBalance().getCurrencyCode());
        Assert.assertEquals(
                BigDecimal.valueOf(777.33),
                Objects.requireNonNull(result).getExactAvailableBalance().getExactValue());
        Assert.assertEquals(
                BigDecimal.valueOf(5432.33),
                Objects.requireNonNull(result).getExactBalance().getExactValue());
        Assert.assertEquals(
                "EUR", Objects.requireNonNull(result).getExactBalance().getCurrencyCode());
    }

    private String getAccount() {
        return "{\"accounts\":["
                + "{\"resourceId\":\"6103210e-d01e-4b53-adfc-01d53485a1ff\","
                + "\"iban\":\"EE382200221020145685\","
                + "\"currency\":\"EUR\","
                + "\"product\":\"Multi currency account\","
                + "\"cashAccountType\":\"CACC\","
                + "\"name\":\"My account\","
                + "\"_links\":"
                + "{\"balances\":"
                + "{\"href\":\"https://api.lhv.eu/psd2/v1/accounts/6103210e-d01e-4b53-adfc-01d53485a1ff/balances\"},"
                + "\"transactions\":"
                + "{\"href\":\"https://api.lhv.eu/psd2/v1/accounts/6103210e-d01e-4b53-adfc-01d53485a1ff/transactions\"}}}]}";
    }

    private String getBalance() {
        return "{\"account\":"
                + "{\"iban\":\"EE382200221020145685\"},"
                + "\"balances\":["
                + "{\"balanceType\":\"interimAvailable\","
                + "\"balanceAmount\":"
                + "{\"currency\":\"EUR\",\"amount\":\"777.33\"}"
                + "},"
                + "{\"balanceType\":\"interimBooked\","
                + "\"balanceAmount\":"
                + "{\"currency\":\"EUR\","
                + "\"amount\":\"5432.33\"}}]}";
    }

    private String getProfiles() {
        return "{\"scaStatus\":\"FINALISED\","
                + "\"authorisationCode\":\"t9ByLn\","
                + "\"selectedRole\":"
                + "{\"id\":\"EE49304030050\","
                + "\"name\":\"Jane Doe\"},"
                + "\"availableRoles\":"
                + "[{\"id\":\"EE49304030050\","
                + "\"name\":\"Jane Doe\"}]}";
    }
}
