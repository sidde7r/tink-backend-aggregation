package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transferdestination;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities.Answer;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.enums.AccountIdentifierType;

@Slf4j
public class SkandiaBankenTransferDestinationFetcher implements TransferDestinationFetcher {

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {

        return new TransferDestinationsResponse(
                accounts.stream()
                        .filter(this::canExecuteExternalTransfer)
                        .collect(
                                Collectors.toMap(
                                        account -> account,
                                        this::getDestinationPatternsForAccount)));
    }

    private boolean canExecuteExternalTransfer(Account account) {
        if (!(AccountTypes.CHECKING.equals(account.getType())
                || AccountTypes.SAVINGS.equals(account.getType()))) {
            return false;
        }

        AccountCapabilities capabilities = account.getCapabilities();

        if (capabilities == null) {
            log.warn(
                    "Account capabilities was not set. "
                            + "Transfer destinations pattern won't be added to the account.");
            return false;
        }
        return Answer.YES.equals(capabilities.getCanExecuteExternalTransfer());
    }

    private List<TransferDestinationPattern> getDestinationPatternsForAccount(Account account) {
        return AccountTypes.CHECKING.equals(account.getType())
                ? getTransferAndGiroDestinations()
                : getTransferDestinations();
    }

    private List<TransferDestinationPattern> getTransferDestinations() {
        return Collections.singletonList(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE));
    }

    private List<TransferDestinationPattern> getTransferAndGiroDestinations() {
        return Arrays.asList(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE),
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE_BG),
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE_PG));
    }
}
