package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;

@JsonObject
public class CurrentEntity {
    @JsonIgnore
    private Logger logger = LoggerFactory.getLogger(CurrentEntity.class);

    @JsonProperty("current")
    private AccountEntity accountEntity;

    public AccountEntity getAccountEntity() {
        return accountEntity;
    }

    public AccountTypes getAccountType() {
        switch (accountEntity.getAccountType()) {

        case SantanderConstants.ACCOUNT_TYPE.CHECKING_ACCOUNT:
            return AccountTypes.CHECKING;

        default:
            logger.error("{} Unknown account type: {}",
                    SantanderConstants.LOGTAG.SANTANDER_UNKNOWN_ACCOUNTTYPE, accountEntity.getAccountType());
            return AccountTypes.OTHER;
        }
    }

    public TransactionalAccount toTransactionalAccount() {
        return TransactionalAccount.builder(getAccountType(), accountEntity.getAccountNumberSort(),
                accountEntity.getAvailableBalance().toTinkAmount())
                .setAccountNumber(accountEntity.getAccountNumberSort())
                .setName(accountEntity.getAccountAlias())
                .build();
    }
}
