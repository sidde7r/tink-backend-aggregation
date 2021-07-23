package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import jakarta.xml.bind.annotation.XmlElement;

public class PmtInf {

    @XmlElement(name = "PmtInfId")
    private String pmtInfId;

    @XmlElement(name = "PmtMtd")
    private String pmtMtd = "TRF";

    @XmlElement(name = "NbOfTxs")
    private int nbOfTxs = 1;

    @XmlElement(name = "CtrlSum")
    private String ctrlSum;

    @XmlElement(name = "ReqdExctnDt")
    private String reqdExctnDt;

    @XmlElement(name = "Dbtr")
    private Dbtr dbtr = new Dbtr("NOT_PROVIDED");

    @XmlElement(name = "DbtrAcct")
    private DbtrAcct dbtrAcct;

    @XmlElement(name = "DbtrAgt")
    private DbtrAgt dbtrAgt;

    @XmlElement(name = "ChrgBr")
    private String chrgBr = "SLEV";

    @XmlElement(name = "CdtTrfTxInf")
    private CdtTrfTxInf cdtTrfTxInf;

    public PmtInf() {}

    public PmtInf(
            CdtTrfTxInf cdtTrfTxInf,
            DbtrAcct dbtrAcct,
            DbtrAgt dbtrAgt,
            String reqdExctnDt,
            String pmtInfId,
            String ctrlSum) {
        this.cdtTrfTxInf = cdtTrfTxInf;
        this.dbtrAcct = dbtrAcct;
        this.dbtrAgt = dbtrAgt;
        this.reqdExctnDt = reqdExctnDt;
        this.pmtInfId = pmtInfId;
        this.ctrlSum = ctrlSum;
    }
}
