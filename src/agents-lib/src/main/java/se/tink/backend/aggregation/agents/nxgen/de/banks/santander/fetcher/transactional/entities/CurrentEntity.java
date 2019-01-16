package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class CurrentEntity {
  @JsonIgnore private Logger logger = LoggerFactory.getLogger(CurrentEntity.class);

  @JsonProperty("current")
  private AccountEntity accountEntity;

  public AccountEntity getAccountEntity() {
    return accountEntity;
  }

  public boolean isValid() {
    try {
      toTransactionalAccount();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isTransactionalAccount(){
    return SantanderConstants.ACCOUNT_TYPE_MAPPER.isTransactionalAccount(accountEntity.getAccountType());
  }

  public TransactionalAccount toTransactionalAccount() {
    return TransactionalAccount.builder(
            SantanderConstants.ACCOUNT_TYPE_MAPPER.translate(accountEntity.getAccountType()).get(),
            accountEntity.getAccountNumberSort(),
            accountEntity.getAvailableBalance().toTinkAmount())
        .setAccountNumber(accountEntity.getAccountNumberSort())
        .setName(accountEntity.getAccountAlias())
        .putInTemporaryStorage(
            SantanderConstants.STORAGE.LOCAL_CONTRACT_TYPE,
            accountEntity.getAccountNumber().getLocalContractType())
        .putInTemporaryStorage(
            SantanderConstants.STORAGE.LOCAL_CONTRACT_DETAIL,
            accountEntity.getAccountNumber().getLocalContractDetail())
        .putInTemporaryStorage(
            SantanderConstants.STORAGE.COMPANY_ID,
            accountEntity.getSubProductEntity().getProductEntity().getCompanyId())
        .build();
  }
}
