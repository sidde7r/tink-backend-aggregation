package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Overview")
public class SecurityAccountOverviewDetailsEntity {
    @XmlAttribute(name = "SecurityAccount")
    private String number;
    @XmlAttribute(name = "TotalValue")
    private String amount;

    @XmlAttribute(name = "CurrentPage")
    private String currentPage;
    @XmlAttribute(name = "NbElemMaxByPage")
    private String maxElementsByPage;

    @XmlAttribute(name = "MaxPage")
    private String maxPage;

    public String getNumber() {
        return number;
    }

    public String getAmount() {
        return amount;
    }
}
