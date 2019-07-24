package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountsEntity {
    private String resourceId;

    private String iban;

    private String bban;

    private String currency;

    private OwnerNameEntity owner;

    private String ownerName;

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

    @JsonProperty("_links")
    private LinksEntity links;

    public TransactionalAccount toTinkAccount() {
        if (product.toLowerCase().contains(SebCommonConstants.ACCOUNT_TYPES.SAVINGS)) {
            return parseAccount(TransactionalAccountType.from(AccountTypes.SAVINGS));
        } else {
            return parseAccount(TransactionalAccountType.from(AccountTypes.CHECKING));
        }
    }

    private TransactionalAccount parseAccount(TransactionalAccountType accountType) {
        return TransactionalAccount.nxBuilder()
                .withType(accountType)
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(bban)
                                .withAccountNumber(bban)
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.SE, bban, ownerName))
                                .addIdentifier(new SwedishIdentifier(bban))
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .addHolderName(getOwnerName())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(bban)
                .putInTemporaryStorage(SebCommonConstants.StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    public boolean isEnabled() {
        return status.equalsIgnoreCase(SebCommonConstants.Accounts.STATUS_ENABLED);
    }

    private String getOwnerName() {
        return Strings.isNullOrEmpty(ownerName) ? owner.getName() : ownerName;
    }

    private Amount getAvailableBalance() {

        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalancesEntity::isAvailableBalance)
                .findFirst()
                .map(BalancesEntity::toAmount)
                .orElse(BalancesEntity.getDefault());
    }

    private String getName() {
        return Strings.isNullOrEmpty(name) ? bban : name;
    }
}
