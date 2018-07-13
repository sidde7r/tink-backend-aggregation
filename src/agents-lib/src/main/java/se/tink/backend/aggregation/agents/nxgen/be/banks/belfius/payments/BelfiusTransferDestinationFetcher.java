package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        ArrayList<BeneficiariesContacts> beneficiaries = apiClient.prepareTransfer().getBeneficiaries();
        TransferDestinationsResponse transferDestinations = new TransferDestinationsResponse();
        transferDestinations.addDestinations(getAccountDestinations(accounts, beneficiaries));
        return transferDestinations;
    }

    private Map<Account, List<TransferDestinationPattern>> getAccountDestinations(Collection<Account> accounts,
            ArrayList<BeneficiariesContacts> beneficiaries) {
        List<GeneralAccountEntity> formatted = getSourceAccounts(accounts);
        List<GeneralAccountEntity> destinationAccounts = getDestinationAccounts(beneficiaries);

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(formatted)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(accounts)
                .matchDestinationAccountsOn(AccountIdentifier.Type.BE, BelgianIdentifier.class)
                .addMultiMatchPattern(AccountIdentifier.Type.BE, TransferDestinationPattern.ALL)
                .build();
    }

    private List<GeneralAccountEntity> getSourceAccounts(Collection<Account> accounts) {
        return accounts.stream()
                .map(this::toGeneralAccountEntity)
                .collect(Collectors.toList());
    }

    private GeneralAccountEntity toGeneralAccountEntity(Account account) {
        AccountIdentifier accountIdentifier =
                new BelgianIdentifier(account.getAccountNumber().replace(" ", ""));

        return new TransferAccountEntity(accountIdentifier, BelfiusConstants.TRANSACTION_BANK_NAME, account.getName());
    }

    private List<GeneralAccountEntity> getDestinationAccounts(ArrayList<BeneficiariesContacts> beneficiaries) {
        return beneficiaries.stream()
                .filter(beneficiary -> beneficiary.generalGetAccountIdentifier() != null)
                .collect(Collectors.toList());
    }

}
