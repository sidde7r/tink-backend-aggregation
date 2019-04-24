package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
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
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(bban)
                .setBalance(getAvailableBalance())
                .setAlias(getName())
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.SE, bban))
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .addHolderName(getOwnerName())
                .setApiIdentifier(bban)
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
