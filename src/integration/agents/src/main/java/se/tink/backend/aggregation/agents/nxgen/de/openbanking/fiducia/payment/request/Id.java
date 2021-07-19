package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import jakarta.xml.bind.annotation.XmlElement;

public class Id {
    @XmlElement(name = "OrgId")
    private OrgId orgId;

    public Id() {}

    public Id(OrgId orgId) {
        this.orgId = orgId;
    }
}
