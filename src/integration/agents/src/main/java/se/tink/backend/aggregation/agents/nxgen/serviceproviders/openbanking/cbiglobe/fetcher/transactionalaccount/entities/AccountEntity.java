package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

    private String bban;
    private String iban;
    private String resourceId;
    private String name;
    private String cashAccountType;

    public AccountEntity(String iban) {
        this.iban = iban;
    }

    public String getIban() {
        return iban;
    }

    public String getResourceId() {
        return resourceId;
    }

    public Optional<TransactionalAccount> toTinkAccount(GetBalancesResponse getBalancesResponse) {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        CbiGlobeConstants.ACCOUNT_TYPE_MAPPER,
                        Optional.ofNullable(cashAccountType).orElse("CASH"),
                        TransactionalAccountType.CHECKING)
                .withBalance(getBalanceModule(getBalancesResponse))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(getName())
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }

    private BalanceModule getBalanceModule(GetBalancesResponse getBalancesResponse) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder()
                        .withBalance(getBookedBalance(getBalancesResponse.getBalances()));
        Optional<ExactCurrencyAmount> availableBalance =
                getAvailableBalance(getBalancesResponse.getBalances());
        availableBalance.ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance(List<BalanceEntity> balances) {
        return balances.stream()
                .filter(
                        balanceEntity ->
                                AvailableBalanceType.SUPPORTED_TYPES.contains(
                                        balanceEntity.getBalanceType()))
                .min(Comparator.comparing(BalanceEntity::getAvailableBalanceMappingPriority))
                .map(BalanceEntity::toAmount);
    }

    private ExactCurrencyAmount getBookedBalance(List<BalanceEntity> balances) {
        return balances.stream()
                .filter(
                        balanceEntity ->
                                BookedBalanceType.SUPPORTED_TYPES.contains(
                                        balanceEntity.getBalanceType()))
                .min(Comparator.comparing(BalanceEntity::getBookedBalanceMappingPriority))
                .map(BalanceEntity::toAmount)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.BALANCE_NOT_FOUND));
    }

    private String getName() {
        return Optional.ofNullable(name).orElse(iban);
    }

    private String getAccountNumber() {
        return Optional.ofNullable(iban).orElse(bban);
    }

    public boolean isEmptyAccountObject() {
        return iban == null || resourceId == null;
    }
}
