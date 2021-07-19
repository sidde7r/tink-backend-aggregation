package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import jakarta.xml.bind.annotation.XmlElement;

public class TransactionDetailsEntity {

    @XmlElement(name = "RltdPties")
    private RelatedPartiesEntity relatedParties;

    @XmlElement(name = "RmtInf")
    private RemittanceInformationEntity remittanceInformation;

    public RelatedPartiesEntity getRelatedParties() {
        return relatedParties;
    }

    public RemittanceInformationEntity getRemittanceInformation() {
        return remittanceInformation;
    }
}
