package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants.Policies;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.rpc.IcaDestinationType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.rpc.IcaSourceType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class IcaBankenTransferDestinationFetcher implements TransferDestinationFetcher {
    private final IcaBankenApiClient apiClient;
    private final IcaBankenSessionStorage sessionStorage;

    private List<AccountEntity> accountEntities;
    private List<RecipientEntity> recipientEntities;
    private Collection<Account> tinkAccounts;

    public IcaBankenTransferDestinationFetcher(
            IcaBankenApiClient apiClient, IcaBankenSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        TransferDestinationsResponse transferDestinations = new TransferDestinationsResponse();
        this.tinkAccounts = accounts;

        if (sessionStorage.hasPolicy(Policies.ACCOUNTS)
                && sessionStorage.hasPolicy(Policies.PAYMENTS)) {
            this.accountEntities = apiClient.fetchAccounts().getTransferSourceAccounts();
            this.recipientEntities = apiClient.fetchDestinationAccounts();

            transferDestinations.addDestinations(getTransferAccountDestinations());
            transferDestinations.addDestinations(getPaymentAccountDestinations());
        }

        return transferDestinations;
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferAccountDestinations() {

        List<GeneralAccountEntity> sourceAccounts = findSourceAccountsFor(IcaSourceType.TRANSFER);
        List<GeneralAccountEntity> destinationAccounts =
                getAllDestinationAccountsFor(IcaDestinationType.TRANSFER);

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(this.tinkAccounts)
                .addMultiMatchPattern(AccountIdentifierType.SE, TransferDestinationPattern.ALL)
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getPaymentAccountDestinations() {
        List<GeneralAccountEntity> sourceAccounts = findSourceAccountsFor(IcaSourceType.PAYMENT);
        List<GeneralAccountEntity> destinationAccounts =
                findDestinationAccountsFor(IcaDestinationType.PAYMENT);

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(this.tinkAccounts)
                .addMultiMatchPattern(AccountIdentifierType.SE_BG, TransferDestinationPattern.ALL)
                .addMultiMatchPattern(AccountIdentifierType.SE_PG, TransferDestinationPattern.ALL)
                .build();
    }

    private List<GeneralAccountEntity> getAllDestinationAccountsFor(
            IcaDestinationType destinationType) {
        List<GeneralAccountEntity> destinationAccounts =
                findDestinationAccountsFor(destinationType);

        Collection<GeneralAccountEntity> additionalDestinationAccounts =
                accountEntities.stream()
                        .filter(ae -> destinationType.contains(ae.getValidFor()))
                        .collect(Collectors.toList());

        destinationAccounts.addAll(additionalDestinationAccounts);

        return destinationAccounts;
    }

    private List<GeneralAccountEntity> findDestinationAccountsFor(
            IcaDestinationType destinationType) {
        if (this.recipientEntities == null) {
            return Collections.emptyList();
        }

        return this.recipientEntities.stream()
                .filter(recipientEntity -> destinationType.contains(recipientEntity.getType()))
                .collect(Collectors.toList());
    }

    private List<GeneralAccountEntity> findSourceAccountsFor(IcaSourceType sourceType) {
        return this.accountEntities.stream()
                .filter(accountEntity -> sourceType.contains(accountEntity.getValidFor()))
                .collect(Collectors.toList());
    }
}
