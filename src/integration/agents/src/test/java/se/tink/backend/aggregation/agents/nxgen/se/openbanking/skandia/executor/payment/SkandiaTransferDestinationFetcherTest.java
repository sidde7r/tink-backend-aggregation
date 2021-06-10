package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transfersdestinations.SkandiaTransferDestinationFetcher;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class SkandiaTransferDestinationFetcherTest {
    private SkandiaTransferDestinationFetcher destinationFetcher;

    @Before
    public void setup() {
        destinationFetcher = new SkandiaTransferDestinationFetcher();
    }

    @Test
    public void testTransferDestinationFetcherWithMultipleAccounts() {
        List<Account> accounts = getAccounts();

        Map<Account, List<TransferDestinationPattern>> transferDestinationsResponse =
                destinationFetcher.fetchTransferDestinationsFor(accounts).getDestinations();

        assertThat(transferDestinationsResponse.get(accounts.get(0)))
                .isEqualTo(getDomesticGirosTransferDestinations());
        assertThat(transferDestinationsResponse.get(accounts.get(1)))
                .isEqualTo(getDomesticTransferDestinations());
    }

    private List<Account> getAccounts() {
        List<Account> accountList = new ArrayList<>();

        Account checking = new Account();
        checking.setBankId("12345");
        checking.setAccountNumber("1234");
        checking.setType(AccountTypes.CHECKING);
        accountList.add(checking);

        Account savings = new Account();
        savings.setBankId("11221");
        savings.setAccountNumber("1122");
        savings.setType(AccountTypes.SAVINGS);
        accountList.add(savings);

        Account empty = new Account();
        empty.setBankId("54321");
        empty.setAccountNumber("4321");
        accountList.add(empty);

        return accountList;
    }

    private List<TransferDestinationPattern> getDomesticTransferDestinations() {
        return Collections.singletonList(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE));
    }

    private List<TransferDestinationPattern> getDomesticGirosTransferDestinations() {
        return Arrays.asList(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE),
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE_BG),
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE_PG));
    }
}
