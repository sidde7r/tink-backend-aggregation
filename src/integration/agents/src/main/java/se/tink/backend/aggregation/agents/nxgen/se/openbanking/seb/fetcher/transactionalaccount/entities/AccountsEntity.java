package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants;
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
        .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.SE, bban))
        .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
        .addHolderName(getOwnerName())
        .setAlias(getName())
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
    return balances != null
        ? balances.stream()
            .filter(BalancesEntity::isAvailableBalance)
            .findFirst()
            .orElse(new BalancesEntity())
            .toAmount()
        : BalancesEntity.Default;
  }

  private String getName() {
    return Strings.isNullOrEmpty(name) ? bban : name;
  }
}
