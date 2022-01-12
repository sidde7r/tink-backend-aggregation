package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.LinksDtoBase;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ArkeaTrustedBeneficiariesLinksEntity implements LinksDtoBase {

    private Href self;

    @JsonProperty("parent-list")
    private Href parentList;

    private Href next;
}
