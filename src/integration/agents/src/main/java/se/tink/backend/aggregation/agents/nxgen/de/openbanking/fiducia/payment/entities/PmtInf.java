package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities;

import javax.xml.bind.annotation.XmlElement;

public class PmtInf {
    @XmlElement(name = "CdtTrfTxInf")
    private CdtTrfTxInf cdtTrfTxInf;

    @XmlElement(name = "PmtTpInf")
    private PmtTpInf pmtTpInf;

    @XmlElement(name = "DbtrAcct")
    private DbtrAcct dbtrAcct;

    @XmlElement(name = "ReqdExctnDt")
    private String reqdExctnDt;

    @XmlElement(name = "ChrgBr")
    private String chrgBr;

    @XmlElement(name = "PmtInfId")
    private String pmtInfId;

    @XmlElement(name = "CtrlSum")
    private String ctrlSum;

    @XmlElement(name = "Dbtr")
    private Dbtr dbtr;

    @XmlElement(name = "NbOfTxs")
    private String nbOfTxs;

    @XmlElement(name = "PmtMtd")
    private String pmtMtd;

    public PmtInf() {}

    public PmtInf(
            CdtTrfTxInf cdtTrfTxInf,
            PmtTpInf pmtTpInf,
            DbtrAcct dbtrAcct,
            String reqdExctnDt,
            String chrgBr,
            String pmtInfId,
            String ctrlSum,
            Dbtr dbtr,
            String nbOfTxs,
            String pmtMtd) {
        this.cdtTrfTxInf = cdtTrfTxInf;
        this.pmtTpInf = pmtTpInf;
        this.dbtrAcct = dbtrAcct;
        this.reqdExctnDt = reqdExctnDt;
        this.chrgBr = chrgBr;
        this.pmtInfId = pmtInfId;
        this.ctrlSum = ctrlSum;
        this.dbtr = dbtr;
        this.nbOfTxs = nbOfTxs;
        this.pmtMtd = pmtMtd;
    }

    public DbtrAcct getDbtrAcct() {
        return dbtrAcct;
    }

    public CdtTrfTxInf getCdtTrfTxInf() {
        return cdtTrfTxInf;
    }
}
