package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SEBConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountsEntity {
    @JsonProperty
    private String resourceId;

    @JsonProperty
    private String iban;

    @JsonProperty
    private String bban;

    @JsonProperty
    private String currency;

    @JsonProperty
    private OwnerNameEntity owner;

    @JsonProperty
    private String ownerName;

    @JsonProperty
    private List<BalancesEntity> balances;

    @JsonProperty
    private String creditLine;

    @JsonProperty
    private String product;

    @JsonProperty
    private String name;

    @JsonProperty
    private String status;

    @JsonProperty
    private String statusDate;

    @JsonProperty
    private String bic;

    @JsonProperty
    private String bicAddress;

    @JsonProperty
    private String accountInterest;

    @JsonProperty
    private boolean cardLinkedToTheAccount;

    @JsonProperty
    private boolean paymentService;

    @JsonProperty
    private String bankgiroNumber;

    @JsonProperty
    private OwnerNameEntity accountOwners;

    @JsonProperty("_links")
    private LinksEntity links;

    public TransactionalAccount toTinkAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(bban)
                .setBalance(getAvailableBalance())
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.SE, bban))
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .addHolderName(getOwnerName())
                .setAlias(getName())
                .setApiIdentifier(bban)
                .putInTemporaryStorage(SEBConstants.STORAGE.ACCOUNT_ID, resourceId)
                .build();
    }

    public boolean isEnabled() {
        return status.equalsIgnoreCase(SEBConstants.ACCOUNTS.STATUS_ENABLED);
    }

    private String getOwnerName() {
        return Strings.isNullOrEmpty(ownerName) ? owner.getName() : ownerName;
    }

    private Amount getAvailableBalance() {
        return balances != null ? balances.stream()
                .filter(BalancesEntity::isAvailableBalance)
                .findFirst()
                .orElse(new BalancesEntity())
                .toAmount() : BalancesEntity.Default;
    }

    private String getName() {
        return Strings.isNullOrEmpty(name) ? bban : name;
    }
}
