package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SecurityAccountOverview")
@XmlAccessorType(XmlAccessType.FIELD)
public class SecurityAccountOverviewEntity {

    @XmlElement(name = "Overview")
    private SecurityAccountOverviewDetailsEntity overview;

    public SecurityAccountOverviewDetailsEntity getOverview() {
        return overview;
    }
}
