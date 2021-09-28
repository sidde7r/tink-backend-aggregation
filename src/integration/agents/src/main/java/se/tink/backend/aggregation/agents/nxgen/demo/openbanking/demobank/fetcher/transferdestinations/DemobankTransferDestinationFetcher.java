package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transferdestinations;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class DemobankTransferDestinationFetcher implements TransferDestinationFetcher {

    private final TransferDestinationPattern defaultPattern =
            TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.IBAN);
    private final TransferDestinationPattern seGeneralPattern =
            TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE);
    private final List<TransferDestinationPattern> seDestinationPatterns =
            unmodifiableList(
                    asList(
                            TransferDestinationPattern.createForMultiMatchAll(
                                    AccountIdentifierType.SE_BG),
                            TransferDestinationPattern.createForMultiMatchAll(
                                    AccountIdentifierType.SE_PG)));

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {

        return new TransferDestinationsResponse(
                accounts.stream()
                        .filter(
                                account ->
                                        (AccountTypes.CHECKING.equals(account.getType())
                                                || AccountTypes.SAVINGS.equals(account.getType())))
                        .collect(
                                Collectors.toMap(
                                        account -> account,
                                        this::getAllAccountIdentifierTypesOrDefault)));
    }

    private List<TransferDestinationPattern> getAllAccountIdentifierTypesOrDefault(
            Account account) {

        Set<TransferDestinationPattern> transferDestinationPatterns =
                ofNullable(account.getIdentifiers()).orElse(emptyList()).stream()
                        .map(AccountIdentifier::getType)
                        .map(TransferDestinationPattern::createForMultiMatchAll)
                        .collect(Collectors.toSet());

        if (transferDestinationPatterns.contains(seGeneralPattern)) {
            transferDestinationPatterns.addAll(seDestinationPatterns);
        }

        if (transferDestinationPatterns.isEmpty()) {
            transferDestinationPatterns.add(defaultPattern);
        }

        return new ArrayList<>(transferDestinationPatterns);
    }
}
