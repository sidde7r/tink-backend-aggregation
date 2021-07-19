package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import jakarta.xml.bind.annotation.XmlElement;

public class MessagePaginationEntity {
    @XmlElement(name = "PgNb")
    private int pageNumber;

    @XmlElement(name = "LastPgInd")
    private boolean lastPageIndex;
}
