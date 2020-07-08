package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transferdestination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntityImpl;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transferdestination.entity.TransferCreditorIdentity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transferdestination.rpc.BeneficiariesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class CaisseEpargneTransferDestinationsFetcher implements TransferDestinationFetcher {
    private final CaisseEpargneApiClient apiClient;

    public CaisseEpargneTransferDestinationsFetcher(CaisseEpargneApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        BeneficiariesResponse response = apiClient.getBeneficiaries();
        List<GeneralAccountEntity> destinationAccounts = response.getDestinationAccounts();
        List<GeneralAccountEntity> sourceAccounts = getSourceAccounts(accounts, response);
        TransferDestinationsResponse transferDestinationsResponse =
                new TransferDestinationsResponse();
        transferDestinationsResponse.addDestinations(
                new TransferDestinationPatternBuilder()
                        .matchDestinationAccountsOn(Type.IBAN, IbanIdentifier.class)
                        .setTinkAccounts(accounts)
                        .setDestinationAccounts(destinationAccounts)
                        .setSourceAccounts(sourceAccounts)
                        .build());
        return transferDestinationsResponse;
    }

    private List<GeneralAccountEntity> getSourceAccounts(
            Collection<Account> accounts, BeneficiariesResponse response) {
        List<GeneralAccountEntity> sourceAccounts = new ArrayList<>();
        List<TransferCreditorIdentity> ownAccounts = response.getOwnAccounts();
        accounts.forEach(
                account -> {
                    Optional<TransferCreditorIdentity> own =
                            ownAccounts.stream()
                                    .filter(
                                            ownAccount ->
                                                    ownAccount
                                                            .getReference()
                                                            .equals(account.getAccountNumber()))
                                    .findFirst();
                    sourceAccounts.add(
                            new GeneralAccountEntityImpl(
                                    own.orElse(new TransferCreditorIdentity()).getBankLabel(),
                                    account.getName(),
                                    account.getIdentifier(Type.IBAN)));
                });
        return sourceAccounts;
    }
}
