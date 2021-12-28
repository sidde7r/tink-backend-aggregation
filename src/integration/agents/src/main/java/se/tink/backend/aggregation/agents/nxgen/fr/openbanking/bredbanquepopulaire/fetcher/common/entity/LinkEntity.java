package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.common.entity;

import lombok.Data;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class LinkEntity {
    private Href href;
    private Boolean templated;
}
