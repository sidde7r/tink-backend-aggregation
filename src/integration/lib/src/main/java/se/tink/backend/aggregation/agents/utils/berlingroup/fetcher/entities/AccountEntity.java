package se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String product;
    private String ownerName;
    private String name;
    private String cashAccountType;

    @JsonProperty("_links")
    private LinksEntity links;
}
