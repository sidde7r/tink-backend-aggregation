package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transfersdestinations.LansforsakringarTransferDestinationFetcher;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class LansforsakringarTransferDestinationFetcherTest {

    private LansforsakringarApiClient apiClient;
    private LansforsakringarTransferDestinationFetcher lansforsakringarTransferDestinationFetcher;

    @Before
    public void setup() {
        apiClient = mock(LansforsakringarApiClient.class);
        lansforsakringarTransferDestinationFetcher =
                new LansforsakringarTransferDestinationFetcher(apiClient);
    }

    @Test
    public void testTransferDestinationFetcherWithMultipleAccounts() {

        when(apiClient.getAccountNumbers())
                .thenReturn(
                        Optional.of(
                                AccountNumbersUtil.getDomesticAccountNumbersResponse(
                                        "12345", "1234", "4321")));

        List<Account> accounts = getAccounts();
        TransferDestinationsResponse transferDestinationsResponse =
                lansforsakringarTransferDestinationFetcher.fetchTransferDestinationsFor(accounts);

        Map<Account, List<TransferDestinationPattern>> destinations =
                transferDestinationsResponse.getDestinations();
        assertThat(destinations.size()).isEqualTo(2);

        List<TransferDestinationPattern> listForFirstAccount = new ArrayList<>();
        listForFirstAccount.add(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE));
        assertThat(destinations.get(accounts.get(0))).isEqualTo(listForFirstAccount);

        List<TransferDestinationPattern> listForSecondAccount = new ArrayList<>();
        listForSecondAccount.add(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE));
        listForSecondAccount.add(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE_BG));
        listForSecondAccount.add(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE_PG));
        assertThat(destinations.get(accounts.get(1))).isEqualTo(listForSecondAccount);
    }

    private List<Account> getAccounts() {
        List<Account> accountList = new ArrayList<>();

        Account a1 = new Account();
        a1.setBankId("12345");
        a1.setAccountNumber("1234");
        accountList.add(a1);

        Account a2 = new Account();
        a2.setBankId("54321");
        a2.setAccountNumber("4321");
        accountList.add(a2);

        return accountList;
    }
}
