package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class CurrentEntity {

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

    public boolean isTransactionalAccount() {
        return SantanderConstants.ACCOUNT_TYPE_MAPPER.isOneOf(
                accountEntity.getAccountType(), TransactionalAccount.ALLOWED_ACCOUNT_TYPES);
    }

    public TransactionalAccount toTransactionalAccount() {
        return TransactionalAccount.builder(
                        SantanderConstants.ACCOUNT_TYPE_MAPPER
                                .translate(accountEntity.getAccountType())
                                .get(),
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
