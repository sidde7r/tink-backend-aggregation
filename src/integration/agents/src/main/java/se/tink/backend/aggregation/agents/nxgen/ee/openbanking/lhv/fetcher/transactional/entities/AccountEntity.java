package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc.SelectedRole;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.BalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Data
@Getter
public class AccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String product;
    private String cashAccountType;
    private String name;

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(
            LhvApiClient apiClient, SessionStorage sessionStorage) {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(LhvConstants.ACCOUNT_TYPE_MAPPER, cashAccountType)
                .withBalance(getBalanceModule(getBalances(apiClient)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(getAccountName())
                                .addIdentifier(new IbanIdentifier(LhvConstants.BIC, iban))
                                .build())
                .addHolderName(getHolderName(sessionStorage))
                .setApiIdentifier(resourceId)
                .build();
    }

    private String getHolderName(SessionStorage sessionStorage) {
        List<SelectedRole> availableRoles =
                sessionStorage
                        .get(
                                StorageKeys.AVAILABLE_ROLES,
                                new TypeReference<List<SelectedRole>>() {})
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Available roles is not found on session storage"));
        return availableRoles.stream().findFirst().get().getName();
    }

    private String getAccountName() {
        return Strings.isNullOrEmpty(name) ? iban : name;
    }

    private List<BalanceEntity> getBalances(LhvApiClient apiClient) {
        BalanceResponse balanceResponse = apiClient.fetchAccountBalance(getResourceId());
        return balanceResponse.getBalances();
    }

    private ExactCurrencyAmount getBookedBalance(List<BalanceEntity> balances) {
        return balances.stream()
                .filter(BalanceEntity::isBooked)
                .findFirst()
                .map(BalanceEntity::toTinkAmount)
                .orElse(getAvailableBalanceIfBookedIsNull(balances));
    }

    private ExactCurrencyAmount getAvailableBalanceIfBookedIsNull(List<BalanceEntity> balances) {
        return balances.stream()
                .filter(BalanceEntity::isAvailable)
                .findFirst()
                .map(BalanceEntity::toTinkAmount)
                .orElseThrow(() -> new IllegalStateException("No balance found in the response"));
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance(List<BalanceEntity> balances) {
        return balances.stream()
                .filter(BalanceEntity::isAvailable)
                .findFirst()
                .map(BalanceEntity::toTinkAmount);
    }

    private BalanceModule getBalanceModule(List<BalanceEntity> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(getBookedBalance(balances));
        getAvailableBalance(balances).ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }
}
