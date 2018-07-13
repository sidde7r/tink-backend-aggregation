package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts.entities.OwnAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.rpc.IcaDestinationType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.rpc.IcaSourceType;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.libraries.account.AccountIdentifier;

public class IcaBankenTransferDestinationFetcher implements TransferDestinationFetcher {
    private final IcaBankenApiClient apiClient;

    public IcaBankenTransferDestinationFetcher(IcaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {

        List<OwnAccountsEntity> accountEntities = apiClient.requestAccountsBody().getAccounts().getOwnAccounts();
        List<RecipientEntity> recipientEntities = apiClient.fetchDestinationAccounts();

        TransferDestinationsResponse transferDestinations = new TransferDestinationsResponse();
        Map<Account, List<TransferDestinationPattern>> bankTransferAccountDestinations = getBankTransferAccountDestinations(
                accounts, accountEntities, recipientEntities);

        transferDestinations.addDestinations(bankTransferAccountDestinations);
        transferDestinations
                .addDestinations(getPaymentAccountDestinations(accounts, accountEntities, recipientEntities));

        return transferDestinations;
    }

    public Map<Account, List<TransferDestinationPattern>> getBankTransferAccountDestinations(
            Collection<Account> updatedAccounts, List<OwnAccountsEntity> ownAccountsEntity,
            List<RecipientEntity> recipientEntities) {
        List<GeneralAccountEntity> sourceAccounts = findSourceAccountsFor(IcaSourceType.TRANSFER, ownAccountsEntity);
        List<GeneralAccountEntity> destinationAccounts = getAllDestinationAccountsFor(IcaDestinationType.TRANSFER,
                ownAccountsEntity, recipientEntities);

        Map<Account, List<TransferDestinationPattern>> transferAccountsDestinations = (new TransferDestinationPatternBuilder())
                .setSourceAccounts(sourceAccounts).setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(updatedAccounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE, TransferDestinationPattern.ALL).build();

        return transferAccountsDestinations;
    }

    public List<GeneralAccountEntity> getAllDestinationAccountsFor(IcaDestinationType destinationType,
            List<OwnAccountsEntity> ownAccountsEntity, List<RecipientEntity> recipientEntities) {
        List<GeneralAccountEntity> destinationAccounts = findDestinationAccountsFor(destinationType, recipientEntities);

        Collection<GeneralAccountEntity> additionalDestinationAccounts = ownAccountsEntity.stream()
                .filter(accountEntity -> destinationType.contains(accountEntity.getValidFor()))
                .collect(Collectors.toList());
        destinationAccounts.addAll(additionalDestinationAccounts);
        return destinationAccounts;
    }

    public Map<Account, List<TransferDestinationPattern>> getPaymentAccountDestinations(Collection<Account> accounts,
            List<OwnAccountsEntity> accountEntities, List<RecipientEntity> recipientEntities) {
        List<GeneralAccountEntity> sourceAccounts = findSourceAccountsFor(IcaSourceType.PAYMENT, accountEntities);
        List<GeneralAccountEntity> destinationAccounts = findDestinationAccountsFor(IcaDestinationType.PAYMENT,
                recipientEntities);

        Map<Account, List<TransferDestinationPattern>> paymentAccountDestinations = (new TransferDestinationPatternBuilder())
                .setSourceAccounts(sourceAccounts).setDestinationAccounts(destinationAccounts).setTinkAccounts(accounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_BG, TransferDestinationPattern.ALL)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_PG, TransferDestinationPattern.ALL).build();
        return paymentAccountDestinations;
    }

    public List<GeneralAccountEntity> findDestinationAccountsFor(IcaDestinationType destinationType,
            List<RecipientEntity> recipientEntities) {

        List<GeneralAccountEntity> destinationAccountsFor = new ArrayList<>();
        if (recipientEntities == null) {
            return destinationAccountsFor;
        }
        for (RecipientEntity recipients : recipientEntities) {
            if (destinationType.contains(recipients.getType())) {
                destinationAccountsFor.add(recipients);
            }
        }

        return destinationAccountsFor;
    }

    public List<GeneralAccountEntity> findSourceAccountsFor(IcaSourceType sourceType,
            List<OwnAccountsEntity> OwnAccountsEntity) {
        return OwnAccountsEntity.stream().filter(accountEntity -> sourceType.contains(accountEntity.getValidFor()))
                .collect(Collectors.toList());
    }
}
