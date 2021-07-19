package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

public class AmountEntity {
    @XmlAttribute(name = "Ccy")
    private String currency;

    @XmlValue private String value;

    public String getCurrency() {
        return currency;
    }

    public String getValue() {
        return value;
    }
}
