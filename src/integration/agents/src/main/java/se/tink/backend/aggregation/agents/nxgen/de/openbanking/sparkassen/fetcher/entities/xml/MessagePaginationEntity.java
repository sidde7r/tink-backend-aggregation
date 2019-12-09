package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
// XML: MsgPgntn
public class MessagePaginationEntity {
    @XmlElement(name = "PgNb")
    private int pageNumber;

    @XmlElement(name = "LastPgInd")
    private boolean lastPageIndex;
}
