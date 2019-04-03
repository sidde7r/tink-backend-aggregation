package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.accountbalances;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccountBalancesDataEntity {

    private List<AccountBalanceEntity> balance;

    public List<AccountBalanceEntity> getBalance() {
        return balance;
    }

    public AccountBalanceEntity getInterimAvailableBalance(){
        return balance.stream()
            .filter(AccountBalanceEntity::isInterimAvailable)
            .findAny().orElseThrow(
                () -> new IllegalStateException(CrosskeyBaseConstants.Exceptions.MISSING_BALANCE));
    }
}
