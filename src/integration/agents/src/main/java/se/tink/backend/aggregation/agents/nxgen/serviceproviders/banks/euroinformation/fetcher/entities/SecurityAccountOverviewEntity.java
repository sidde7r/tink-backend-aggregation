package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SecurityAccountOverview")
@XmlAccessorType(XmlAccessType.FIELD)
public class SecurityAccountOverviewEntity {

    @XmlElement(name = "Overview")
    private SecurityAccountOverviewDetailsEntity overview;

    public SecurityAccountOverviewDetailsEntity getOverview() {
        return overview;
    }
}
