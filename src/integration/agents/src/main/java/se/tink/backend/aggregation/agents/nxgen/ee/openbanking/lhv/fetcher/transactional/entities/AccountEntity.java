package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc.SelectedRole;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Data
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
                .withType(getAccountType(cashAccountType))
                .withPaymentAccountFlag()
                .withBalance(getBalanceModule(apiClient))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .addHolderName(getHolderName(sessionStorage))
                .setApiIdentifier(resourceId)
                .build();
    }

    @JsonIgnore
    private TransactionalAccountType getAccountType(String accountType) {
        return accountType.toLowerCase().contains(AccountTypes.CURRENT)
                ? TransactionalAccountType.CHECKING
                : TransactionalAccountType.SAVINGS;
    }

    @JsonIgnore
    public ExactCurrencyAmount getAvailableBalance(LhvApiClient apiClient, String resourceId) {
        BalanceResponse balanceResponse = apiClient.fetchAccountBalance(resourceId);

        return Optional.ofNullable(balanceResponse.getBalances()).orElse(Collections.emptyList())
                .stream()
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .map(BalanceEntity::toAmount)
                .orElseThrow(() -> new IllegalStateException("could not get balance"));
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

    private BalanceModule getBalanceModule(LhvApiClient apiClient) {
        BalanceResponse balanceResponse = apiClient.fetchAccountBalance(resourceId);
        List<BalanceEntity> balances = balanceResponse.getBalances();
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(BalanceMapper.getBookedBalance(balances));

        BalanceMapper.getAvailableBalance(balances)
                .ifPresent(balanceBuilderStep::setAvailableBalance);

        return balanceBuilderStep.build();
    }
}
