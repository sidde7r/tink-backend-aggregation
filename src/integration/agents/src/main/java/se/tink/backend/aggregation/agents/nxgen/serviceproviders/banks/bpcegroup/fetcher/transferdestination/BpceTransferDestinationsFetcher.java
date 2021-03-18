package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.fetcher.transferdestination;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.transferdestination.BeneficiariesResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.transferdestination.BeneficiaryItemDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.transferdestination.TransferCreditorIdentityDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.entity.BeneficiaryEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@RequiredArgsConstructor
public class BpceTransferDestinationsFetcher implements TransferDestinationFetcher {

    private final BpceApiClient bpceApiClient;

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        return new TransferDestinationsResponse(getTransferAccountDestinations(accounts));
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferAccountDestinations(
            Collection<Account> accounts) {

        final BeneficiariesResponseDto beneficiariesResponseDto = bpceApiClient.getBeneficiaries();
        final List<GeneralAccountEntity> ownAccountList =
                getSourceAccounts(accounts, beneficiariesResponseDto);
        final List<GeneralAccountEntity> destinationAccountList =
                getDestinationAccounts(beneficiariesResponseDto);

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(ownAccountList)
                .setDestinationAccounts(destinationAccountList)
                .setTinkAccounts(accounts)
                .matchDestinationAccountsOn(AccountIdentifierType.IBAN, IbanIdentifier.class)
                .build();
    }

    private static List<GeneralAccountEntity> getSourceAccounts(
            Collection<Account> accounts, BeneficiariesResponseDto beneficiariesResponseDto) {

        final List<TransferCreditorIdentityDto> ownAccounts =
                getOwnAccounts(beneficiariesResponseDto);

        return accounts.stream()
                .map(account -> getGeneralAccountEntity(ownAccounts, account))
                .collect(Collectors.toList());
    }

    private static List<GeneralAccountEntity> getDestinationAccounts(
            BeneficiariesResponseDto beneficiariesResponseDto) {
        return beneficiariesResponseDto.getItems().stream()
                .map(BeneficiaryItemDto::getTransferCreditorIdentity)
                .filter(BpceTransferDestinationsFetcher::isActivated)
                .filter(BpceTransferDestinationsFetcher::isDestinationAccount)
                .map(
                        BpceTransferDestinationsFetcher
                                ::convertTransferCreditorIdentityDtoToBeneficiaryEntity)
                .collect(Collectors.toList());
    }

    private static List<TransferCreditorIdentityDto> getOwnAccounts(
            BeneficiariesResponseDto beneficiariesResponseDto) {
        return beneficiariesResponseDto.getItems().stream()
                .map(BeneficiaryItemDto::getTransferCreditorIdentity)
                .filter(BpceTransferDestinationsFetcher::isOwnAccount)
                .collect(Collectors.toList());
    }

    private static GeneralAccountEntity getGeneralAccountEntity(
            List<TransferCreditorIdentityDto> ownAccounts, Account account) {
        final Optional<TransferCreditorIdentityDto> maybeTransferCreditorId =
                ownAccounts.stream()
                        .filter(
                                ownAccount ->
                                        ownAccount
                                                .getReference()
                                                .equalsIgnoreCase(account.getAccountNumber()))
                        .findAny();

        return maybeTransferCreditorId
                .map(
                        creditorId ->
                                convertTransferCreditorIdentityDtoToBeneficiaryEntity(
                                        creditorId, account))
                .orElseGet(() -> convertAccountToBeneficiaryEntity(account, null));
    }

    private static boolean isActivated(TransferCreditorIdentityDto transferCreditorIdentityDto) {
        return StringUtils.isEmpty(transferCreditorIdentityDto.getActivationDate());
    }

    private static boolean isOwnAccount(TransferCreditorIdentityDto transferCreditorIdentityDto) {
        return transferCreditorIdentityDto.isHolderIndicator();
    }

    private static boolean isDestinationAccount(
            TransferCreditorIdentityDto transferCreditorIdentityDto) {
        return !isOwnAccount(transferCreditorIdentityDto);
    }

    private static BeneficiaryEntity convertTransferCreditorIdentityDtoToBeneficiaryEntity(
            TransferCreditorIdentityDto transferCreditorIdentityDto) {
        return new BeneficiaryEntity(
                new IbanIdentifier(transferCreditorIdentityDto.getIban()),
                transferCreditorIdentityDto.getBankLabel(),
                transferCreditorIdentityDto.getDesignationLabel());
    }

    private static BeneficiaryEntity convertTransferCreditorIdentityDtoToBeneficiaryEntity(
            TransferCreditorIdentityDto transferCreditorIdentityDto, Account account) {
        return convertAccountToBeneficiaryEntity(
                account, transferCreditorIdentityDto.getBankLabel());
    }

    private static BeneficiaryEntity convertAccountToBeneficiaryEntity(
            Account account, String bankLabel) {
        return new BeneficiaryEntity(
                account.getIdentifier(AccountIdentifierType.IBAN), bankLabel, account.getName());
    }
}
