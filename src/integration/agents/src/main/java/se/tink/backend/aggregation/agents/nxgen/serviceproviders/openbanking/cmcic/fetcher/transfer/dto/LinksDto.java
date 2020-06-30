package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transfer.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.LinksDtoBase;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LinksDto implements LinksDtoBase {

    private Href first;

    private Href last;

    private Href next;

    private Href parentList;

    private Href prev;

    private Href self;
}
