package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.spankki.fetcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class SPankkiFITransferDestinationFetcher implements TransferDestinationFetcher {

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        Map<Account, List<TransferDestinationPattern>> transferDestinations = new HashMap<>();
        accounts.stream()
                .filter(
                        account ->
                                (AccountTypes.CHECKING.equals(account.getType())
                                        || AccountTypes.SAVINGS.equals(account.getType())))
                .forEach(
                        account ->
                                transferDestinations.put(
                                        account, getDomesticTransferDestinations()));
        return new TransferDestinationsResponse(transferDestinations);
    }

    private static List<TransferDestinationPattern> getDomesticTransferDestinations() {
        return Arrays.asList(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.IBAN),
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.FI));
    }
}
