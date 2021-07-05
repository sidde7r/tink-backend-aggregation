package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import jakarta.xml.bind.annotation.XmlElement;

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

    public String getCreditDebitIndicator() {
        return creditDebitIndicator;
    }

    public String getStatus() {
        return status;
    }

    public DateEntity getBookingDate() {
        return bookingDate;
    }

    public DateEntity getValueDate() {
        return valueDate;
    }

    public EntryDetailsEntity getEntryDetails() {
        return entryDetails;
    }
}
