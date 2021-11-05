package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class LineDetailEntity {
    @JsonProperty("identification")
    private DocumentLineIdentificationEntity identification = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("amount")
    private RemittanceAmountEntity amount = null;
}
