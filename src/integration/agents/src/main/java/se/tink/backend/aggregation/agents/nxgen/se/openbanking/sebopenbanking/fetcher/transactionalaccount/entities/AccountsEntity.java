package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountsEntity {
    private String resourceId;
    private String iban;
    private String bban;
    private String currency;
    private OwnerNameEntity owner;
    private String ownerName;
    private String OwnerId;
    private List<BalancesEntity> balances;
    private String creditLine;
    private String product;
    private String name;
    private String status;
    private String statusDate;
    private String bic;
    private String bicAddress;
    private String accountInterest;
    private boolean cardLinkedToTheAccount;
    private boolean paymentService;
    private String bankgiroNumber;
    private OwnerNameEntity accountOwners;
    private List<AliasesEntity> aliases;
    private LimitsEntity limits;

    @JsonProperty("_links")
    private LinksEntity links;

    public Optional<TransactionalAccount> toTinkAccount() {
        return (product.toLowerCase().contains(AccountTypes.SAVINGS))
                ? parseAccount(TransactionalAccountType.SAVINGS)
                : parseAccount(TransactionalAccountType.CHECKING);
    }

    private Optional<TransactionalAccount> parseAccount(TransactionalAccountType accountType) {
        return TransactionalAccount.nxBuilder()
                .withType(accountType)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(bban)
                                .withAccountNumber(bban)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .addHolderName(getOwnerName())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(bban)
                .putInTemporaryStorage(SebCommonConstants.StorageKeys.ACCOUNT_ID, resourceId)
                .putPayload(SebCommonConstants.StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    public boolean isEnabled() {
        return status.equalsIgnoreCase(SebCommonConstants.Accounts.STATUS_ENABLED);
    }

    private String getOwnerName() {
        return Strings.isNullOrEmpty(ownerName) ? owner.getName() : ownerName;
    }

    private ExactCurrencyAmount getAvailableBalance() {

        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalancesEntity::isAvailableBalance)
                .findFirst()
                .map(BalancesEntity::toAmount)
                .orElseThrow(() -> new IllegalStateException("Could not get balance"));
    }

    private String getName() {
        return Strings.isNullOrEmpty(name) ? bban : name;
    }
}
