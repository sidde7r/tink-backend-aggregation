package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsEntity {
    @JsonProperty("product")
    private List<AccountEntity> accountsList;

    public List<AccountEntity> getAccountsList() {
        return Optional.ofNullable(accountsList).orElse(Collections.emptyList());
    }
}
