package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.XmlConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EntryEntity {

    @XmlElement(name = "Amt")
    private AmountEntity amount;

    @XmlElement(name = "CdtDbtInd")
    private String creditDebitIndicator;

    @XmlElement(name = "Sts")
    private String status;

    @XmlElement(name = "BookgDt")
    private DateEntity bookingDate;

    @XmlElement(name = "ValDt")
    private DateEntity valueDate;

    @XmlElement(name = "NtryDtls")
    private EntryDetailsEntity entryDetails;

    public AmountEntity getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public DateEntity getValueDate() {
        return valueDate;
    }

    public String getDescription() {
        return entryDetails.getTransactionDetails().getRemittanceInformation().getUnstructured();
    }

    public boolean isBooked() {
        return XmlConstants.BOOKED.equalsIgnoreCase(status);
    }

    public boolean isDebit() {
        return XmlConstants.DBIT.equalsIgnoreCase(creditDebitIndicator);
    }
}
