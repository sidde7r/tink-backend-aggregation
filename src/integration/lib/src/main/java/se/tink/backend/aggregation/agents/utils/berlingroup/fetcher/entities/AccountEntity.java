package se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Setter
public class AccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String product;
    private String ownerName;
    private String name;
    private String cashAccountType;

    private List<BalanceEntity> balances;

    @JsonProperty("_links")
    private FetcherLinksEntity links;
}
