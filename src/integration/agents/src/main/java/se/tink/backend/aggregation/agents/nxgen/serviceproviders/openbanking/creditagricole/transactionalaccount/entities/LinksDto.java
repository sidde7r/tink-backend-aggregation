package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import lombok.Data;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.LinksDtoBase;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class LinksDto implements LinksDtoBase {

    private Href first;

    private Href last;

    private Href next;

    private Href parentist;

    private Href prev;

    private Href self;
}
