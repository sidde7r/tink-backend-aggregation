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
    private String chrgBr = "SLEV";

    @XmlElement(name = "PmtInfId")
    private String pmtInfId;

    @XmlElement(name = "CtrlSum")
    private String ctrlSum;

    @XmlElement(name = "Dbtr")
    private Dbtr dbtr = new Dbtr("NOT_PROVIDED");

    @XmlElement(name = "NbOfTxs")
    private int nbOfTxs = 1;

    @XmlElement(name = "PmtMtd")
    private String pmtMtd = "TRF";

    public PmtInf() {}

    public PmtInf(
            CdtTrfTxInf cdtTrfTxInf,
            DbtrAcct dbtrAcct,
            String reqdExctnDt,
            String pmtInfId,
            String ctrlSum) {
        this.cdtTrfTxInf = cdtTrfTxInf;
        this.dbtrAcct = dbtrAcct;
        this.reqdExctnDt = reqdExctnDt;
        this.pmtInfId = pmtInfId;
        this.ctrlSum = ctrlSum;
    }
}
