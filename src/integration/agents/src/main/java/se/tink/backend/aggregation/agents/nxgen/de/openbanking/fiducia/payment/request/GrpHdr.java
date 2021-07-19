package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import jakarta.xml.bind.annotation.XmlElement;

public class GrpHdr {
    @XmlElement(name = "MsgId")
    private String msgId;

    @XmlElement(name = "CreDtTm")
    private String creDtTm;

    @XmlElement(name = "NbOfTxs")
    private int nbOfTxs = 1;

    @XmlElement(name = "CtrlSum")
    private String ctrlSum;

    @XmlElement(name = "InitgPty")
    private InitgPty initgPty;

    public GrpHdr() {}

    public GrpHdr(String creDtTm, String ctrlSum, InitgPty initgPty, String msgId) {
        this.creDtTm = creDtTm;
        this.ctrlSum = ctrlSum;
        this.initgPty = initgPty;
        this.msgId = msgId;
    }
}
