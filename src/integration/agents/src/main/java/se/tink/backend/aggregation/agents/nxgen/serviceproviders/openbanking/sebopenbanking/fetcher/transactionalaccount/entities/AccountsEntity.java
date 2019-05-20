package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.assertj.core.util.Strings;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.Amount;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.from(AccountTypes.CHECKING))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(bban)
                                .withAccountName(name)
                                .addIdentifier(new SwedishIdentifier(bban))
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .addHolderName(getOwnerName())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(bban)
                .putInTemporaryStorage(SebConstants.StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    public boolean isEnabled() {
        return status.equalsIgnoreCase(SebConstants.Accounts.STATUS_ENABLED);
    }

    private String getOwnerName() {
        return Strings.isNullOrEmpty(ownerName) ? owner.getName() : ownerName;
    }

    private Amount getAvailableBalance() {

        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalancesEntity::isAvailableBalance)
                .findFirst()
                .map(BalancesEntity::toAmount)
                .orElse(BalancesEntity.Default);
    }

    private String getName() {
        return Strings.isNullOrEmpty(name) ? bban : name;
    }
}
