package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.transfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntityImpl;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.LinkDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetTrustedBeneficiariesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.transfer.entity.BeneficiaryEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@RequiredArgsConstructor
public class CreditAgricoleTransferDestinationFetcher implements TransferDestinationFetcher {

    private final CreditAgricoleBaseApiClient creditAgricoleApiClient;

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
        Optional<GetTrustedBeneficiariesResponse> response =
                creditAgricoleApiClient.getTrustedBeneficiaries();
        Optional<String> maybeNextPagePath;

        while (response.isPresent()
                && (maybeNextPagePath =
                                processTrustedBeneficiariesResponse(
                                        response.get(), trustedBeneficiariesAccounts))
                        .isPresent()) {
            response = creditAgricoleApiClient.getTrustedBeneficiaries(maybeNextPagePath.get());
        }

        return trustedBeneficiariesAccounts;
    }

    private List<GeneralAccountEntity> getAccountEntityList(Collection<Account> accounts) {
        return accounts.stream()
                .map(CreditAgricoleTransferDestinationFetcher::accountToGeneralAccountEntity)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<? extends GeneralAccountEntity> accountToGeneralAccountEntity(
            Account account) {
        return GeneralAccountEntityImpl.createFromCoreAccount(account);
    }

    private static Optional<String> processTrustedBeneficiariesResponse(
            GetTrustedBeneficiariesResponse response,
            List<GeneralAccountEntity> beneficiariesAccounts) {

        response.getBeneficiaries().stream()
                .map(BeneficiaryEntity::from)
                .forEach(beneficiariesAccounts::add);

        return Optional.ofNullable(response.getLinks().getNext()).map(LinkDetailsEntity::getHref);
    }
}
