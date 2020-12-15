package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
@Getter
public class AccessEntity {

    private String allPsd2;
    private String availableAccountsWithBalances;

    public AccessEntity(String allPsd2) {
        this.allPsd2 = allPsd2;
    }

    public AccessEntity() {
        this.availableAccountsWithBalances = "allAccounts";
    }
}
