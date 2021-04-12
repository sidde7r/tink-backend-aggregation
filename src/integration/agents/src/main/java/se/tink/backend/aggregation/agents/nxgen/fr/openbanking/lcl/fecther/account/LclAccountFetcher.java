package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.account;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.AccountResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.AccountUsage;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.BalanceResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.BalanceType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.CashAccountType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.converter.LclDataConverter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RequiredArgsConstructor
@Slf4j
public class LclAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final LclApiClient apiClient;
    private final LclDataConverter dataConverter;

    @Override
    public List<TransactionalAccount> fetchAccounts() {
        AccountsResponseDto accountsResponseDto = apiClient.getAccountsResponse();

        if (accountsResponseDto.getAccounts().stream()
                .filter(acc -> CashAccountType.CACC != acc.getCashAccountType())
                .findFirst()
                .isPresent()) {
            log.info("Account type different than CACC.");
        }

        return accountsResponseDto.getAccounts().stream()
                .filter(account -> account.getCashAccountType() == CashAccountType.CACC)
                .map(this::mapAccountResourceToTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> mapAccountResourceToTransactionalAccount(
            AccountResourceDto account) {
        final String iban = account.getAccountId().getIban();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(getBalanceModule(account.getBalances()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(account.getName())
                                .addIdentifier(new IbanIdentifier(account.getBicFi(), iban))
                                .setProductName(account.getProduct())
                                .build())
                .setApiIdentifier(account.getResourceId())
                .setHolderType(getAccountHolderType(account))
                .build();
    }

    private BalanceModule getBalanceModule(List<BalanceResourceDto> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(getBookedBalance(balances));
        getAvailableBalance(balances).ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }

    private ExactCurrencyAmount getBookedBalance(List<BalanceResourceDto> balances) {
        if (balances.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine booked balance from empty list of balances.");
        }
        Optional<BalanceResourceDto> balanceEntity =
                balances.stream().filter(b -> BalanceType.CLBD == b.getBalanceType()).findAny();

        if (!balanceEntity.isPresent()) {
            log.warn(
                    "Couldn't determine booked balance of known type, and no credit limit included. Defaulting to first provided balance.");
        }
        return balanceEntity
                .map(Optional::of)
                .orElseGet(() -> balances.stream().findFirst())
                .map(BalanceResourceDto::getBalanceAmount)
                .map(dataConverter::convertAmountDtoToExactCurrencyAmount)
                .get();
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance(List<BalanceResourceDto> balances) {
        return balances.stream()
                .filter(b -> BalanceType.XPCD == b.getBalanceType())
                .findAny()
                .map(BalanceResourceDto::getBalanceAmount)
                .map(dataConverter::convertAmountDtoToExactCurrencyAmount);
    }

    private AccountHolderType getAccountHolderType(AccountResourceDto account) {
        return (account.getUsage() == AccountUsage.PRIV)
                ? AccountHolderType.PERSONAL
                : AccountHolderType.BUSINESS;
    }
}
