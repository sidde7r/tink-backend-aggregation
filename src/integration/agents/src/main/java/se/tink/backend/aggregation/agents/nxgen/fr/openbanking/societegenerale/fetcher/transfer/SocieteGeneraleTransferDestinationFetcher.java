package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntityImpl;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transfer.entity.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transfer.rpc.TrustedBeneficiariesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@RequiredArgsConstructor
public class SocieteGeneraleTransferDestinationFetcher implements TransferDestinationFetcher {

    private final SocieteGeneraleApiClient societeGeneraleApiClient;

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        return new TransferDestinationsResponse(getTransferAccountDestinations(accounts));
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferAccountDestinations(
            Collection<Account> accounts) {
        final List<GeneralAccountEntity> ownAccountList = getAccountEntityList(accounts);
        final List<GeneralAccountEntity> destinationAccountList =
                getDestinationAccountList(ownAccountList);

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(ownAccountList)
                .setDestinationAccounts(destinationAccountList)
                .setTinkAccounts(accounts)
                .matchDestinationAccountsOn(AccountIdentifier.Type.IBAN, IbanIdentifier.class)
                .addMultiMatchPattern(AccountIdentifier.Type.IBAN, TransferDestinationPattern.ALL)
                .build();
    }

    private List<GeneralAccountEntity> getDestinationAccountList(
            List<GeneralAccountEntity> ownAccounts) {
        final List<GeneralAccountEntity> destinations = getTrustedBeneficiariesAccounts();

        destinations.addAll(ownAccounts);

        return destinations;
    }

    private List<GeneralAccountEntity> getTrustedBeneficiariesAccounts() {
        final List<GeneralAccountEntity> trustedBeneficiariesAccounts = new ArrayList<>();
        TrustedBeneficiariesResponse response = societeGeneraleApiClient.getTrustedBeneficiaries();
        Optional<String> maybeNextPagePath;

        while ((maybeNextPagePath =
                        processTrustedBeneficiariesResponse(response, trustedBeneficiariesAccounts))
                .isPresent()) {
            response = societeGeneraleApiClient.getTrustedBeneficiaries(maybeNextPagePath.get());
        }

        return trustedBeneficiariesAccounts;
    }

    private List<GeneralAccountEntity> getAccountEntityList(Collection<Account> accounts) {
        return accounts.stream()
                .map(SocieteGeneraleTransferDestinationFetcher::accountToGeneralAccountEntity)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<? extends GeneralAccountEntity> accountToGeneralAccountEntity(
            Account account) {
        return GeneralAccountEntityImpl.createFromCoreAccount(account);
    }

    private static Optional<String> processTrustedBeneficiariesResponse(
            TrustedBeneficiariesResponse response,
            List<GeneralAccountEntity> beneficiariesAccounts) {
        response.getBeneficiaries().stream()
                .map(BeneficiaryEntity::from)
                .forEach(beneficiariesAccounts::add);

        return Optional.ofNullable(response.getLinks().getNext()).map(Href::getHref);
    }
}
