package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities;

import javax.xml.bind.annotation.XmlElement;

public class CdtTrfTxInf {
    @XmlElement(name = "Cdtr")
    private Cdtr cdtr;

    @XmlElement(name = "CdtrAcct")
    private CdtrAcct cdtrAcct;

    @XmlElement(name = "PmtId")
    private PmtId pmtId;

    @XmlElement(name = "Amt")
    private Amt amt;

    @XmlElement(name = "RmtInf")
    private RmtInf rmtInf;

    public CdtTrfTxInf() {}

    public CdtTrfTxInf(Cdtr cdtr, CdtrAcct cdtrAcct, PmtId pmtId, Amt amt, RmtInf rmtInf) {
        this.cdtr = cdtr;
        this.cdtrAcct = cdtrAcct;
        this.pmtId = pmtId;
        this.amt = amt;
        this.rmtInf = rmtInf;
    }

    public Amt getAmt() {
        return amt;
    }

    public CdtrAcct getCdtrAcct() {
        return cdtrAcct;
    }
}
