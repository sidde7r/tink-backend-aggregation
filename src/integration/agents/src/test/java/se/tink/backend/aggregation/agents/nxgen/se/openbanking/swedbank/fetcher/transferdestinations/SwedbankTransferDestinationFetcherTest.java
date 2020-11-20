package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transferdestinations;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;

public class SwedbankTransferDestinationFetcherTest {
    @Test
    public void onlyCheckingAccountsGetTransferDestinations() {
        SwedbankTransferDestinationFetcher fetcher = new SwedbankTransferDestinationFetcher();
        TransferDestinationsResponse response = fetcher.fetchTransferDestinationsFor(getAccounts());
        Map<Account, List<TransferDestinationPattern>> destinations = response.getDestinations();
        assertThat(destinations.size()).isEqualTo(1);
        Optional<Account> a = destinations.keySet().stream().findFirst();
        assertThat(a.isPresent()).isEqualTo(true);
        assertThat(a.orElseThrow(IllegalStateException::new).getType())
                .isEqualTo(AccountTypes.CHECKING);
        Optional<List<TransferDestinationPattern>> patterns =
                destinations.values().stream().findFirst();
        assertThat(patterns.isPresent()).isEqualTo(true);
        assertThat(patterns.orElseThrow(IllegalStateException::new).size()).isEqualTo(3);
    }

    private List<Account> getAccounts() {
        List<Account> accountList = new ArrayList<>();
        for (AccountTypes type : AccountTypes.values()) {
            Account a = new Account();
            a.setType(type);
            accountList.add(a);
        }
        return accountList;
    }
}
