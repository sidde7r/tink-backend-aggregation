package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SecurityAccount")
public class SecurityAccountListEntity {

    @XmlAttribute(name = "number")
    private String number;

    @XmlAttribute(name = "AccountDisplayed")
    private String accountDisplayed;

    public String getNumber() {
        return number;
    }

    public String getAccountDisplayed() {
        return accountDisplayed;
    }
}
