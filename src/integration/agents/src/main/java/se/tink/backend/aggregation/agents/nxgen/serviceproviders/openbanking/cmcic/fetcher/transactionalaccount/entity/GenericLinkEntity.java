package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class GenericLinkEntity {
    @JsonProperty("href")
    private String href = null;

    @JsonProperty("templated")
    private Boolean templated = null;
}
