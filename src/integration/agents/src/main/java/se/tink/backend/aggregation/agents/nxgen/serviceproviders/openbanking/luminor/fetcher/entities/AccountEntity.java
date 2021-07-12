package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    @Getter private String iban;
    @Getter private String currency;
    private String name;

    @Getter private String resourceId;

    private String product;
    private String bic;
    @Getter private boolean cardAccount;
    private String interestRate;

    private String usage;

    private String status;

    @JsonProperty("balances")
    private List<BalanceEntity> balanceEntity;

    @JsonProperty("_links")
    LinksEntity links;

    public boolean isEUR() {
        return "EUR".equalsIgnoreCase(currency);
    }

    private String getName() {
        return Strings.isNullOrEmpty(name) ? iban : name;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(String accountHolderName) {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        LuminorConstants.ACCOUNT_TYPE_MAPPER,
                        product,
                        TransactionalAccountType.CHECKING)
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(resourceId)
                                .withAccountName(getName())
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(resourceId)
                .addHolderName(accountHolderName)
                .putInTemporaryStorage(LuminorConstants.StorageKeys.ACCOUNT_ID, iban)
                .build();
    }

    @JsonIgnore
    public ExactCurrencyAmount getBalance() {
        return balanceEntity.stream()
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .map(BalanceEntity::getBalanceAmountEntity)
                .map(BalanceAmountEntity::toAmount)
                .orElseThrow(() -> new IllegalStateException("No balance found in the response"));
    }
}
