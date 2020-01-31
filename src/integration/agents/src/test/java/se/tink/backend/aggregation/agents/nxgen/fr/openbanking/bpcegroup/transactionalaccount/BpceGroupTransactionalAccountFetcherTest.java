package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.converter.BpceGroupTransactionalAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BpceGroupTransactionalAccountFetcherTest {

    private static final String RESOURCE_ID = "009988";

    private BpceGroupTransactionalAccountFetcher bpceGroupTransactionalAccountFetcher;

    private BpceGroupApiClient bpceGroupApiClientMock;

    private BpceGroupTransactionalAccountConverter bpceGroupTransactionalAccountConverterMock;

    @Before
    public void setUp() {
        bpceGroupApiClientMock = mock(BpceGroupApiClient.class);
        when(bpceGroupApiClientMock.fetchBalances(RESOURCE_ID)).thenReturn(getBalancesResponse());

        bpceGroupTransactionalAccountConverterMock =
                mock(BpceGroupTransactionalAccountConverter.class);

        bpceGroupTransactionalAccountFetcher =
                new BpceGroupTransactionalAccountFetcher(
                        bpceGroupApiClientMock, bpceGroupTransactionalAccountConverterMock);
    }

    @Test
    public void shouldFetchAccounts() {
        // given
        when(bpceGroupApiClientMock.fetchAccounts()).thenReturn(getAccountsResponse());
        final TransactionalAccount transactionalAccountMock = mock(TransactionalAccount.class);
        when(bpceGroupTransactionalAccountConverterMock.toTransactionalAccount(any(), any()))
                .thenReturn(Optional.of(transactionalAccountMock));

        // when
        final Collection<TransactionalAccount> result =
                bpceGroupTransactionalAccountFetcher.fetchAccounts();

        // then
        verify(bpceGroupApiClientMock).fetchAccounts();
        verify(bpceGroupApiClientMock).recordCustomerConsent(any());
        verify(bpceGroupApiClientMock).fetchBalances(RESOURCE_ID);
        assertThat(result).containsExactly(transactionalAccountMock);
    }

    @Test
    public void shouldFilterOutNonTransactionalAccounts() {
        // given
        when(bpceGroupApiClientMock.fetchAccounts())
                .thenReturn(getAccountsResponseForCardAccount());

        // when
        final Collection<TransactionalAccount> result =
                bpceGroupTransactionalAccountFetcher.fetchAccounts();

        // then
        verify(bpceGroupApiClientMock).fetchAccounts();
        verify(bpceGroupApiClientMock, never()).recordCustomerConsent(any());
        verify(bpceGroupApiClientMock, never()).fetchBalances(RESOURCE_ID);
        assertThat(result).isEmpty();
    }

    private static AccountsResponse getAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "\"accounts\": [\n"
                        + "    {\n"
                        + "      \"cashAccountType\": \"CACC\",\n"
                        + "      \"accountId\": {\n"
                        + "        \"iban\": \"FR7613807008043001965409135\"\n"
                        + "      },\n"
                        + "      \"resourceId\": \""
                        + RESOURCE_ID
                        + "\",\n"
                        + "      \"name\": \"Account\"\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}",
                AccountsResponse.class);
    }

    private static AccountsResponse getAccountsResponseForCardAccount() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "\"accounts\": [\n"
                        + "    {\n"
                        + "      \"cashAccountType\": \"CARD\",\n"
                        + "      \"accountId\": {\n"
                        + "        \"iban\": \"FR7613807008043001965409135\"\n"
                        + "      },\n"
                        + "      \"resourceId\": \""
                        + RESOURCE_ID
                        + "\",\n"
                        + "      \"name\": \"Card\"\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}",
                AccountsResponse.class);
    }

    private static BalancesResponse getBalancesResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"balances\": [\n"
                        + "    {\n"
                        + "      \"balanceType\": \"VALU\",\n"
                        + "      \"name\": \"Bal1\",\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"amount\": \"4321.95\",\n"
                        + "        \"currency\": \"EUR\"\n"
                        + "      },\n"
                        + "      \"referenceDate\": \"2019-05-16\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"balanceType\": \"CLBD\",\n"
                        + "      \"name\": \"Bal2\",\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"amount\": \"4179.95\",\n"
                        + "        \"currency\": \"EUR\"\n"
                        + "      },\n"
                        + "      \"referenceDate\": \"2019-05-15\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"balanceType\": \"OTHR\",\n"
                        + "      \"name\": \"Bal3\",\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"amount\": \"4348.95\",\n"
                        + "        \"currency\": \"EUR\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}",
                BalancesResponse.class);
    }
}
