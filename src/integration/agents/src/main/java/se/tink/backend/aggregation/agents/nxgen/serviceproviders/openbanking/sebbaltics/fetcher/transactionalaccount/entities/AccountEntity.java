package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String cashAccountType;
    private String ownerName;
    private String name;

    @JsonProperty("_links")
    private LinkEntity links;

    public Optional<TransactionalAccount> toTinkAccount(
            SebBalticsApiClient apiClient, String bicCode) {
        List<BalanceEntity> balances = apiClient.fetchAccountBalances(resourceId).getBalances();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(getBalanceModule(balances))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(bicCode, iban))
                                .build())
                .setApiIdentifier(resourceId)
                .addHolderName(ownerName)
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, resourceId)
                .putPayload(StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    private BalanceModule getBalanceModule(List<BalanceEntity> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(getBookedBalance(balances));
        getAvailableBalance(balances).ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }

    private ExactCurrencyAmount getBookedBalance(List<BalanceEntity> balances) {
        return balances.stream()
                .filter(BalanceEntity::isBookedBalance)
                .findFirst()
                .map(BalanceEntity::toAmount)
                .orElseThrow(() -> new IllegalStateException("Could not get booked balance."));
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance(List<BalanceEntity> balanceResponse) {
        return balanceResponse.stream()
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .map(BalanceEntity::toAmount);
    }
}
