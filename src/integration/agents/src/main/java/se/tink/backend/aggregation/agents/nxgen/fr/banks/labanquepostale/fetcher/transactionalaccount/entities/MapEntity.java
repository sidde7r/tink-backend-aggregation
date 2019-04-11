package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MapEntity {
    @JsonProperty("numero")
    private String number;

    @JsonProperty("codeVarianteProduit")
    private String productVariantCode;

    @JsonProperty("gammeProduit")
    private String productRange;
}
