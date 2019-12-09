package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionDetailsEntity {

    @XmlElement(name = "RmtInf")
    private RemittanceInformationEntity remittanceInformation;

    public RemittanceInformationEntity getRemittanceInformation() {
        return remittanceInformation;
    }
}
