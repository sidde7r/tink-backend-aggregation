package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.creditcard.converter.BpceGroupCreditCardConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BpceGroupCreditCardFetcherTest {

    private BpceGroupCreditCardFetcher fetcher;
    private BpceGroupApiClient apiClient;
    private BpceGroupCreditCardConverter converter;

    @Before
    public void setup() {
        apiClient = mock(BpceGroupApiClient.class);
        converter = mock(BpceGroupCreditCardConverter.class);
        fetcher = new BpceGroupCreditCardFetcher(apiClient, converter);
    }

    @Test
    public void shouldFetchCards() {

        // given
        when(apiClient.fetchAccounts()).thenReturn(getAccountsResponseForCardAccount());
        CreditCardAccount convertedMock = mock(CreditCardAccount.class);
        when(converter.toCreditCardAccount(any(), any())).thenReturn(convertedMock);

        // when
        Collection<CreditCardAccount> result = fetcher.fetchAccounts();

        // then
        assertThat(result).isNotNull().hasSize(1);

        CreditCardAccount card = result.stream().findAny().orElse(null);
        assertThat(card).isEqualTo(convertedMock);

        verify(apiClient).fetchBalances(eq("1234-1234"));
    }

    @Test
    public void shouldFilterOutNonCards() {

        // given
        when(apiClient.fetchAccounts()).thenReturn(getAccountsResponseForCardAccount());
        CreditCardAccount convertedMock = mock(CreditCardAccount.class);
        when(converter.toCreditCardAccount(any(), any())).thenReturn(convertedMock);

        // when
        Collection<CreditCardAccount> result = fetcher.fetchAccounts();

        // then
        assertThat(result).isNotNull().hasSize(1);

        CreditCardAccount card = result.stream().findAny().orElse(null);
        assertThat(card).isEqualTo(convertedMock);

        verify(apiClient).fetchBalances(eq("1234-1234"));
        verify(apiClient, never()).fetchBalances(eq("1234-4567"));
    }

    private static AccountsResponse getAccountsResponseForCardAccount() {
        return SerializationUtils.deserializeFromString(
                "{"
                        + "\"accounts\": ["
                        + "    {"
                        + "      \"cashAccountType\": \"CARD\","
                        + "      \"accountId\": {"
                        + "        \"iban\": \"FR7613807008043001965409135\","
                        + "        \"other\": {"
                        + "          \"identification\": \"1234XXXX1234\","
                        + "          \"schemeName\": \"CPAN\""
                        + "        }"
                        + "      },"
                        + "      \"resourceId\": \"1234-1234\","
                        + "      \"name\": \"Card\","
                        + "      \"_links\": {}"
                        + "    }"
                        + "  ]"
                        + "}",
                AccountsResponse.class);
    }

    private static AccountsResponse getMultipleAccounts() {
        return SerializationUtils.deserializeFromString(
                "{"
                        + "\"accounts\": ["
                        + "    {"
                        + "      \"cashAccountType\": \"CARD\","
                        + "      \"accountId\": {"
                        + "        \"iban\": \"FR7613807008043001965409135\","
                        + "        \"other\": {"
                        + "          \"identification\": \"1234XXXX1234\","
                        + "          \"schemeName\": \"CPAN\""
                        + "        }"
                        + "      },"
                        + "      \"resourceId\": \"1234-1234\","
                        + "      \"name\": \"Card\","
                        + "      \"_links\": {}"
                        + "    },"
                        + "    {"
                        + "      \"cashAccountType\": \"CACC\","
                        + "      \"accountId\": {"
                        + "        \"iban\": \"FR7613807008043001965409135\""
                        + "      },"
                        + "      \"resourceId\": \"1234-4567\","
                        + "      \"name\": \"An account\","
                        + "      \"_links\": {}"
                        + "    }"
                        + "  ]"
                        + "}",
                AccountsResponse.class);
    }
}
