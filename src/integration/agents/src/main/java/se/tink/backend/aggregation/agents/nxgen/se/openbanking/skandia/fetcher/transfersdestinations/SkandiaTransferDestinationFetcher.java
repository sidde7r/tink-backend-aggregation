package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transfersdestinations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class SkandiaTransferDestinationFetcher implements TransferDestinationFetcher {

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        Map<Account, List<TransferDestinationPattern>> transferDestinations =
                accounts.stream()
                        .collect(
                                Collectors.toMap(
                                        account -> account,
                                        SkandiaTransferDestinationFetcher
                                                ::getTransferDestinationsForAccount));

        return new TransferDestinationsResponse(transferDestinations);
    }

    private static List<TransferDestinationPattern> getTransferDestinationsForAccount(
            Account account) {
        return AccountTypes.CHECKING.equals(account.getType())
                ? getDomesticGirosTransferDestinations()
                : getDomesticTransferDestinations();
    }

    private static List<TransferDestinationPattern> getDomesticTransferDestinations() {
        return Collections.singletonList(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE));
    }

    private static List<TransferDestinationPattern> getDomesticGirosTransferDestinations() {
        return Arrays.asList(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE),
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE_BG),
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE_PG));
    }
}
