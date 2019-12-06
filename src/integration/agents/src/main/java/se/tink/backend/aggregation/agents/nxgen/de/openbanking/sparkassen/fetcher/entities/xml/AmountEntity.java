package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
// XML: Amt
public class AmountEntity {
    @XmlAttribute(name = "Ccy")
    private String currency;

    @XmlValue private String value;

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getValue() {
        return new BigDecimal(value);
    }
}
