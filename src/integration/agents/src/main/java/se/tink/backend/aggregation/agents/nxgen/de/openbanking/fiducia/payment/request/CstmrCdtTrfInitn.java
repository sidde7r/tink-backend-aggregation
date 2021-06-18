package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import javax.xml.bind.annotation.XmlElement;

public class CstmrCdtTrfInitn {
    @XmlElement(name = "GrpHdr")
    private GrpHdr grpHdr;

    @XmlElement(name = "PmtInf")
    private PmtInf pmtInf;

    public CstmrCdtTrfInitn() {}

    public CstmrCdtTrfInitn(GrpHdr grpHdr, PmtInf pmtInf) {
        this.grpHdr = grpHdr;
        this.pmtInf = pmtInf;
    }

    public PmtInf getPmtInf() {
        return pmtInf;
    }
}
