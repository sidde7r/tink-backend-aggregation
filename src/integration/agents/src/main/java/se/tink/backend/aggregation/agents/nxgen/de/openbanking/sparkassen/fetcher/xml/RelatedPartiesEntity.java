package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import jakarta.xml.bind.annotation.XmlElement;

public class RelatedPartiesEntity {
    @XmlElement(name = "Cdtr")
    private PartyEntity creditor;

    @XmlElement(name = "Dbtr")
    private PartyEntity debtor;

    public PartyEntity getCreditor() {
        return creditor;
    }

    public PartyEntity getDebtor() {
        return debtor;
    }
}
