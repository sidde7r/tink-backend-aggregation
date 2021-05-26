package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities;

import javax.xml.bind.annotation.XmlElement;

public class PmtTpInf {
    @XmlElement(name = "SvcLvl")
    private SvcLvl svcLvl;

    public PmtTpInf() {}

    public PmtTpInf(SvcLvl svcLvl) {
        this.svcLvl = svcLvl;
    }
}
