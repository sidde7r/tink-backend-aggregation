package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.BeneficiaryDtoBase;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDtoBase;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.entity.BeneficiaryEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@RequiredArgsConstructor
public abstract class FrTransferDestinationFetcher implements TransferDestinationFetcher {

    protected final FrAispApiClient apiClient;

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        return new TransferDestinationsResponse(getTransferAccountDestinations(accounts));
    }

    protected List<GeneralAccountEntity> getTrustedBeneficiariesAccounts() {
        final List<GeneralAccountEntity> trustedBeneficiariesAccounts = new ArrayList<>();
        Optional<? extends TrustedBeneficiariesResponseDtoBase> response =
                apiClient.getTrustedBeneficiaries();
        Optional<String> maybeNextPagePath;

        while (response.isPresent()
                && (maybeNextPagePath =
                                processTrustedBeneficiariesResponse(
                                        response.get(), trustedBeneficiariesAccounts))
                        .isPresent()) {
            response = apiClient.getTrustedBeneficiaries(maybeNextPagePath.get());
        }

        return trustedBeneficiariesAccounts;
    }

    private static Optional<String> processTrustedBeneficiariesResponse(
            TrustedBeneficiariesResponseDtoBase response,
            List<GeneralAccountEntity> beneficiariesAccounts) {

        response.getBeneficiaries().stream()
                .map(FrTransferDestinationFetcher::convertBeneficiaryDtoToEntity)
                .forEach(beneficiariesAccounts::add);

        return Optional.ofNullable(response.getLinks().getNext()).map(Href::getHref);
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
                .matchDestinationAccountsOn(AccountIdentifierType.IBAN, IbanIdentifier.class)
                .addMultiMatchPattern(AccountIdentifierType.IBAN, TransferDestinationPattern.ALL)
                .build();
    }

    private List<GeneralAccountEntity> getDestinationAccountList(
            List<GeneralAccountEntity> ownAccounts) {
        final List<GeneralAccountEntity> destinations = getTrustedBeneficiariesAccounts();

        destinations.addAll(ownAccounts);

        return destinations;
    }

    private List<GeneralAccountEntity> getAccountEntityList(Collection<Account> accounts) {
        return accounts.stream()
                .map(FrTransferDestinationFetcher::accountToGeneralAccountEntity)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<? extends GeneralAccountEntity> accountToGeneralAccountEntity(
            Account account) {
        return GeneralAccountEntityImpl.createFromCoreAccount(account);
    }

    protected static BeneficiaryEntity convertBeneficiaryDtoToEntity(
            BeneficiaryDtoBase beneficiaryDto) {
        return new BeneficiaryEntity(
                new IbanIdentifier(beneficiaryDto.getCreditorAccount().getIban()),
                Objects.nonNull(beneficiaryDto.getCreditorAgent())
                        ? beneficiaryDto.getCreditorAgent().getBicFi()
                        : null,
                beneficiaryDto.getCreditor().getName());
    }
}
