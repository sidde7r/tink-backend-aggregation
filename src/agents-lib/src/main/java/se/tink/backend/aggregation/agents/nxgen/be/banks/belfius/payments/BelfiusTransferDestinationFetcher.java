package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.preparetransfer.BeneficiariesContacts;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BelgianIdentifier;

public class BelfiusTransferDestinationFetcher implements TransferDestinationFetcher {

    private final BelfiusApiClient apiClient;

    public BelfiusTransferDestinationFetcher(BelfiusApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> tinkAccounts) {
        TransferDestinationsResponse transferDestinations = new TransferDestinationsResponse();
        transferDestinations.addDestinations(getToOwnAccountsDestinations(tinkAccounts));
        transferDestinations.addDestinations(getToOtherAccountsDestinations(tinkAccounts));
        return transferDestinations;
    }

    private Map<Account, List<TransferDestinationPattern>> getToOwnAccountsDestinations(
            Collection<Account> tinkAccounts) {

        List<GeneralAccountEntity> sourceAccounts = getAccountsForTransferToOwn(tinkAccounts);
        List<GeneralAccountEntity> destinationAccounts = getOwnAccounts(tinkAccounts);

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(tinkAccounts)
                .matchDestinationAccountsOn(AccountIdentifier.Type.BE, BelgianIdentifier.class)
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getToOtherAccountsDestinations(
            Collection<Account> tinkAccounts) {

        List<GeneralAccountEntity> sourceAccounts = getAccountsForTransferToOther(tinkAccounts);
        List<BeneficiariesContacts> destinationAccounts = apiClient.prepareTransfer().getBeneficiaries();

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(tinkAccounts)
                .matchDestinationAccountsOn(AccountIdentifier.Type.BE, BelgianIdentifier.class)
                .addMultiMatchPattern(AccountIdentifier.Type.BE, TransferDestinationPattern.ALL)
                .build();
    }

    private List<GeneralAccountEntity> getAccountsForTransferToOwn(Collection<Account> accounts) {
        return accounts.stream()
                .filter(ACCOUNTS_FOR_TRANSFER_TO_OWN_PREDICATE)
                .map(this::toGeneralAccountEntity)
                .collect(Collectors.toList());
    }

    private List<GeneralAccountEntity> getAccountsForTransferToOther(Collection<Account> accounts) {
        return accounts.stream()
                .filter(ACCOUNTS_FOR_TRANSFER_TO_OWN_PREDICATE.negate())
                .map(this::toGeneralAccountEntity)
                .collect(Collectors.toList());
    }

    private List<GeneralAccountEntity> getOwnAccounts(Collection<Account> accounts) {
        return accounts.stream()
                .map(this::toGeneralAccountEntity)
                .collect(Collectors.toList());
    }

    private GeneralAccountEntity toGeneralAccountEntity(Account account) {
        AccountIdentifier accountIdentifier =
                new BelgianIdentifier(account.getAccountNumber().replace(" ", ""));

        return new TransferAccountEntity(accountIdentifier, BelfiusConstants.TRANSACTION_BANK_NAME, account.getName());
    }

    private static Predicate<Account> ACCOUNTS_FOR_TRANSFER_TO_OWN_PREDICATE =
            account -> {
                switch (account.getType()) {
                case SAVINGS:
                    return true;
                default:
                    return false;
                }
            };
}
