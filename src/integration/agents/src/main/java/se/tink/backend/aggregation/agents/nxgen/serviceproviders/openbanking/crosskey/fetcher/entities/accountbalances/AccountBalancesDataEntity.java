package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.accountbalances;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccountBalancesDataEntity {

    @JsonProperty("Balance")
    private List<AccountBalanceEntity> balances;

    public List<AccountBalanceEntity> getBalances() {
        return balances;
    }

    public AccountBalanceEntity getInterimAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(AccountBalanceEntity::isInterimAvailable)
                .findAny()
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_BALANCE));
    }
}
