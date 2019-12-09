package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
// XML: Acct
public class AccountEntity {
    @XmlElement(name = "Id")
    private IdEntity id;

    @XmlElement(name = "Ccy")
    private String currency;

    @XmlElement(name = "Svcr")
    private SvcrEntity svcr;
}
