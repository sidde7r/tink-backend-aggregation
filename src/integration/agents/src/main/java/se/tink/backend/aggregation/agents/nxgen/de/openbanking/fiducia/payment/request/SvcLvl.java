package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import jakarta.xml.bind.annotation.XmlElement;

public class SvcLvl {
    @XmlElement(name = "Cd")
    private String cd = "SEPA";
}
