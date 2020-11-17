package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccountEntity {
    private String accountId;

    private String currency;

    @JsonProperty("AccountType")
    private String rawAccountType;

    @JsonProperty("AccountSubType")
    private String rawAccountSubType;

    private String nickname;

    private String description;

    @JsonProperty("Account")
    private List<AccountIdentifierEntity> identifiers = new ArrayList<>();
}
