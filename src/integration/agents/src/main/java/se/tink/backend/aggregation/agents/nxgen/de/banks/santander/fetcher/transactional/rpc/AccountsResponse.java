package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities.AccountResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsResponse {
  @JsonProperty("methodResult")
  private AccountResultEntity result;

  public Collection<TransactionalAccount> toTransactionalAccounts() {
    return result.toTransactionalAccount();
  }

  public AccountResultEntity getAccountResultEntity() {
    return result;
  }

  public boolean containsCreditCards() {
    return result.containsCreditCards();
  }
}
