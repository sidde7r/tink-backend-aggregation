package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.fetcher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.common.types.CashAccountType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.apiclient.ArkeaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity.ArkeaAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity.ArkeaBalanceEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@RequiredArgsConstructor
public class ArkeaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private static final String INSTANT_BALANCE = "XPCD";
    private static final String ACCOUNTING_BALANCE = "CLBD";
    protected final ArkeaApiClient apiClient;

    public Collection<TransactionalAccount> fetchAccounts() {
        return Optional.ofNullable(apiClient.getAccounts().getAccountEntityList())
                .orElse(Collections.emptyList()).stream()
                .filter(this::filterAccountType)
                .map(this::mapToAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private boolean filterAccountType(ArkeaAccountEntity accountEntity) {
        if (CashAccountType.CARD.equals(accountEntity.getCashAccountType())) {
            log.warn("Account of a credit card type");
        }
        return CashAccountType.CACC.equals(accountEntity.getCashAccountType());
    }

    private Optional<TransactionalAccount> mapToAccount(ArkeaAccountEntity accountEntity) {
        List<ArkeaBalanceEntity> balances =
                apiClient.getBalances(accountEntity.getResourceId()).getBalanceEntityList();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(getBalanceModule(balances))
                .withId(getIdModule(accountEntity))
                .setApiIdentifier(accountEntity.getResourceId())
                .build();
    }

    private BalanceModule getBalanceModule(List<ArkeaBalanceEntity> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(getBookedOrDefaultBalance(balances));
        getAvailableBalance(balances).ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }

    private ExactCurrencyAmount getBookedOrDefaultBalance(List<ArkeaBalanceEntity> balances) {
        if (balances.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine booked balance from empty list of balances.");
        }

        Optional<ArkeaBalanceEntity> bookedBalanceEntity =
                balances.stream()
                        .filter(balance -> ACCOUNTING_BALANCE.equals(balance.getBalanceType()))
                        .findAny();

        if (!bookedBalanceEntity.isPresent()) {
            log.warn(
                    "Couldn't determine booked balance of known type, and no credit limit included. Defaulting to first provided balance.");
        }

        return bookedBalanceEntity
                .map(Optional::of)
                .orElseGet(() -> balances.stream().findFirst())
                .map(ArkeaBalanceEntity::getBalanceAmountEntity)
                .map(
                        balanceAmountEntity ->
                                ExactCurrencyAmount.of(
                                        balanceAmountEntity.getAmount(),
                                        balanceAmountEntity.getCurrency()))
                .get();
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance(List<ArkeaBalanceEntity> balances) {
        return balances.stream()
                .filter(balance -> INSTANT_BALANCE.equals(balance.getBalanceType()))
                .findAny()
                .map(ArkeaBalanceEntity::getBalanceAmountEntity)
                .map(
                        balanceAmountEntity ->
                                ExactCurrencyAmount.of(
                                        balanceAmountEntity.getAmount(),
                                        balanceAmountEntity.getCurrency()));
    }

    private IdModule getIdModule(ArkeaAccountEntity accountEntity) {
        return IdModule.builder()
                .withUniqueIdentifier(accountEntity.getAccountId().getIban())
                .withAccountNumber(accountEntity.getAccountId().getIban())
                .withAccountName(accountEntity.getName())
                .addIdentifier(
                        new IbanIdentifier(
                                accountEntity.getBicFi(), accountEntity.getAccountId().getIban()))
                .build();
    }
}
