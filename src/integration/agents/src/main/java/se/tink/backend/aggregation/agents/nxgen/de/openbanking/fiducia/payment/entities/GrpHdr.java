package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities;

import javax.xml.bind.annotation.XmlElement;

public class GrpHdr {
    @XmlElement(name = "CreDtTm")
    private String creDtTm;

    @XmlElement(name = "CtrlSum")
    private String ctrlSum;

    @XmlElement(name = "InitgPty")
    private InitgPty initgPty;

    @XmlElement(name = "NbOfTxs")
    private String nbOfTxs;

    @XmlElement(name = "MsgId")
    private String msgId;

    public GrpHdr() {}

    public GrpHdr(String creDtTm, String ctrlSum, InitgPty initgPty, String nbOfTxs, String msgId) {
        this.creDtTm = creDtTm;
        this.ctrlSum = ctrlSum;
        this.initgPty = initgPty;
        this.nbOfTxs = nbOfTxs;
        this.msgId = msgId;
    }
}
