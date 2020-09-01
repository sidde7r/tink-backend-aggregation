package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountsAndIdentitiesViewEntity {
    @JsonProperty("elencoRapporti")
    private List<AccountEntity> accounts;

    @JsonProperty("elencoClienti")
    private List<IdentityDataEntity> identities;
}
