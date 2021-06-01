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
    private int nbOfTxs = 1;

    @XmlElement(name = "MsgId")
    private String msgId;

    public GrpHdr() {}

    public GrpHdr(String creDtTm, String ctrlSum, InitgPty initgPty, String msgId) {
        this.creDtTm = creDtTm;
        this.ctrlSum = ctrlSum;
        this.initgPty = initgPty;
        this.msgId = msgId;
    }
}
