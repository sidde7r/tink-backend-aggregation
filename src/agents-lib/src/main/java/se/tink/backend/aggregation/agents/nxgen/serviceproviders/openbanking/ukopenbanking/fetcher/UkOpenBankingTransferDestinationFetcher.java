package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntityImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;

public class UkOpenBankingTransferDestinationFetcher implements TransferDestinationFetcher {

    // Whitelist source account types. Basically to avoid displaying credit cards as transfer sources.
    private static final ImmutableList<AccountTypes> WHITELISTED_ACCOUNT_TYPES = ImmutableList.<AccountTypes>builder()
            .add(AccountTypes.CHECKING)
            .add(AccountTypes.SAVINGS)
            .build();

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        TransferDestinationsResponse transferDestinations = new TransferDestinationsResponse();

        Map<Account, List<TransferDestinationPattern>> destinations = new TransferDestinationPatternBuilder()
                .setTinkAccounts(accounts)
                .setDestinationAccounts(Collections.emptyList())
                .setSourceAccounts(getSourceAccounts(accounts))
                .addMultiMatchPattern(AccountIdentifier.Type.SORT_CODE, TransferDestinationPattern.ALL)
                .matchDestinationAccountsOn(AccountIdentifier.Type.SORT_CODE, SortCodeIdentifier.class)
                .build();

        transferDestinations.addDestinations(destinations);
        return transferDestinations;
    }

    private static List<? extends GeneralAccountEntity> getSourceAccounts(Collection<Account> accounts) {
        return accounts.stream()
                .filter(a -> WHITELISTED_ACCOUNT_TYPES.contains(a.getType()))
                .map(GeneralAccountEntityImpl::createFromCoreAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
