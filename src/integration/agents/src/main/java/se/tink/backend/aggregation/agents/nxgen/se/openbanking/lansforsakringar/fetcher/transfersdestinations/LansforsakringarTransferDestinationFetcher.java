package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transfersdestinations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountNumbersResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.enums.AccountIdentifierType;

@AllArgsConstructor
public class LansforsakringarTransferDestinationFetcher implements TransferDestinationFetcher {

    private final LansforsakringarApiClient apiClient;

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {

        AccountNumbersResponse accountNumbers = apiClient.getAccountNumbers();
        Map<Account, List<TransferDestinationPattern>> transferDestinations = new HashMap<>();

        accounts.forEach(
                account -> {
                    Optional<AccountInfoEntity> accountInfoEntity =
                            accountNumbers.findAccountInfoEntity(account.getAccountNumber());

                    accountInfoEntity.ifPresent(
                            a -> {
                                List<TransferDestinationPattern> allowedTransactionTypes =
                                        new ArrayList<>();
                                if (a.isDomesticTransferAllowed()) {
                                    allowedTransactionTypes.addAll(
                                            getDomesticTransferDestinations());
                                }
                                if (a.isDomesticGiroTransferAllowed()) {
                                    allowedTransactionTypes.addAll(
                                            getDomesticGirosTransferDestinations());
                                }
                                transferDestinations.put(account, allowedTransactionTypes);
                            });
                });

        return new TransferDestinationsResponse(transferDestinations);
    }

    public static List<TransferDestinationPattern> getDomesticTransferDestinations() {
        return Collections.singletonList(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE));
    }

    public static List<TransferDestinationPattern> getDomesticGirosTransferDestinations() {
        return Arrays.asList(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE_BG),
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE_PG));
    }
}
