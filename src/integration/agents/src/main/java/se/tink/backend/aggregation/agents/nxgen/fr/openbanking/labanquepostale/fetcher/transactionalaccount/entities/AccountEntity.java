package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Links;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
  private String resourceId;
  private String currency;
  private String name;
  private String cashAccountType;
  private String iban;

  private List<BalanceBaseEntity> balances;

  @JsonProperty("_links")
  AccountLinksEntity links;

  public AccountBaseEntity toBerlinGroupAccountBaseResponse(){
    return new AccountBaseEntity(resourceId, "FR14 2004 1010 0505 0001 3M02 606" , currency, name, cashAccountType,
        new se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountLinksEntity
            (links.getBalances().getHref(), links.getTransactions().getHref()), balances);
  }

  public String getResourceId() {return resourceId;}

  public String getIban() {return iban; }

  public void setIban(String iban) {this.iban = iban;}

  public List<BalanceBaseEntity> getBalances() {return balances;}

  public void setBalances(List<BalanceBaseEntity> balances) {this.balances = balances;}


}
