package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import javax.xml.bind.annotation.XmlElement;

public class TransactionDetailsEntity {

    @XmlElement(name = "RmtInf")
    private RemittanceInformationEntity remittanceInformation;

    public RemittanceInformationEntity getRemittanceInformation() {
        return remittanceInformation;
    }
}
