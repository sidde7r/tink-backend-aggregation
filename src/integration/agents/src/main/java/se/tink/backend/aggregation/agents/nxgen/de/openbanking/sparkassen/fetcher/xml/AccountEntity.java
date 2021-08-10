package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import jakarta.xml.bind.annotation.XmlElement;

public class AccountEntity {
    @XmlElement(name = "Id")
    private IdEntity id;

    @XmlElement(name = "Ccy")
    private String currency;

    @XmlElement(name = "Svcr")
    private SvcrEntity svcr;
}
