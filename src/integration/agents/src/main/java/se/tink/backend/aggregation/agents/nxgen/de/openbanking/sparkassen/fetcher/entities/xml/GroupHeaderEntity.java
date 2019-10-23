package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import se.tink.backend.aggregation.annotations.JsonObject;

// XML: GrpHdr
@JsonObject
public class GroupHeaderEntity {
    @XmlElement(name = "MsgId")
    private String msgId;

    @XmlElement(name = "CreDtTm")
    private String creDtTm;

    @XmlElement(name = "MsgPgntn")
    private MessagePaginationEntity msgPgntn;
}
