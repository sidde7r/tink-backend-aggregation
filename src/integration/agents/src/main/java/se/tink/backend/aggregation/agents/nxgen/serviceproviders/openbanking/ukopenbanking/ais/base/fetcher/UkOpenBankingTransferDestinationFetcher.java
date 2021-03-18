package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntityImpl;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@RequiredArgsConstructor
public class UkOpenBankingTransferDestinationFetcher implements TransferDestinationFetcher {

    private final TransferDestinationAccountsProvider destinationAccountsProvider;

    // Whitelist source account types. Basically to avoid displaying credit cards as transfer
    // sources.
    private static final ImmutableList<AccountTypes> WHITELISTED_ACCOUNT_TYPES =
            ImmutableList.<AccountTypes>builder()
                    .add(AccountTypes.CHECKING)
                    .add(AccountTypes.SAVINGS)
                    .build();

    private static final List<AccountIdentifierType>
            WHITELISTED_TRANSFER_DESTINATION_ACCOUNT_TYPES =
                    ImmutableList.<AccountIdentifierType>builder()
                            .add(AccountIdentifierType.NO)
                            .add(AccountIdentifierType.SE_BG)
                            .add(AccountIdentifierType.BE)
                            .add(AccountIdentifierType.SE)
                            .add(AccountIdentifierType.SE_PG)
                            .add(AccountIdentifierType.SE_SHB_INTERNAL)
                            .add(AccountIdentifierType.IBAN)
                            .add(AccountIdentifierType.SORT_CODE)
                            .add(AccountIdentifierType.FI)
                            .add(AccountIdentifierType.SEPA_EUR)
                            .add(AccountIdentifierType.TINK)
                            .build();

    private final AccountIdentifierType destinationAccountIdentifierType;
    private final Class<? extends AccountIdentifier> destinationAccountIdentifierClass;

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        return new TransferDestinationsResponse(getTransferAccountDestinations(accounts));
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferAccountDestinations(
            Collection<Account> accounts) {
        final List<GeneralAccountEntity> ownAccounts = getAccountEntityList(accounts);
        final List<GeneralAccountEntity> destinations =
                ownAccounts.stream()
                        .filter(
                                destination ->
                                        WHITELISTED_TRANSFER_DESTINATION_ACCOUNT_TYPES.contains(
                                                destination
                                                        .generalGetAccountIdentifier()
                                                        .getType()))
                        .collect(Collectors.toList());
        accounts.forEach(
                account ->
                        destinations.addAll(
                                destinationAccountsProvider.getTrustedBeneficiariesAccounts(
                                        account)));

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(ownAccounts)
                .setDestinationAccounts(destinations)
                .setTinkAccounts(accounts)
                .matchDestinationAccountsOn(
                        destinationAccountIdentifierType, destinationAccountIdentifierClass)
                .addMultiMatchPattern(
                        destinationAccountIdentifierType, TransferDestinationPattern.ALL)
                .build();
    }

    private List<GeneralAccountEntity> getAccountEntityList(Collection<Account> accounts) {
        return accounts.stream()
                .filter(a -> WHITELISTED_ACCOUNT_TYPES.contains(a.getType()))
                .map(GeneralAccountEntityImpl::createFromCoreAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
