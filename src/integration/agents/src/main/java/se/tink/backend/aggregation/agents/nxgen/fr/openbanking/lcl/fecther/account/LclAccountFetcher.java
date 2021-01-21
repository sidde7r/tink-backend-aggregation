package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.account;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
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
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@RequiredArgsConstructor
@Slf4j
public class LclAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private static final List<BalanceType> BALANCE_PREFERRED_TYPES =
            ImmutableList.of(BalanceType.CLBD, BalanceType.XPCD);

    private final LclApiClient apiClient;
    private final PrioritizedValueExtractor prioritizedValueExtractor;
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
                .withBalance(BalanceModule.of(getBalance(account)))
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

    private ExactCurrencyAmount getBalance(AccountResourceDto account) {
        return prioritizedValueExtractor
                .pickByValuePriority(
                        account.getBalances(),
                        BalanceResourceDto::getBalanceType,
                        BALANCE_PREFERRED_TYPES)
                .map(BalanceResourceDto::getBalanceAmount)
                .map(dataConverter::convertAmountDtoToExactCurrencyAmount)
                .orElseThrow(
                        () ->
                                new AccountRefreshException(
                                        "Could not extract account balance. No available balance with type of: "
                                                + StringUtils.join(BALANCE_PREFERRED_TYPES, ", ")));
    }

    private AccountHolderType getAccountHolderType(AccountResourceDto account) {
        return (account.getUsage() == AccountUsage.PRIV)
                ? AccountHolderType.PERSONAL
                : AccountHolderType.BUSINESS;
    }
}
