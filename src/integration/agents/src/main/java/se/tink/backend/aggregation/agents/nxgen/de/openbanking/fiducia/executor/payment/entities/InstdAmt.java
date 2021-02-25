package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class InstdAmt {
    @XmlAttribute(name = "Ccy")
    private String ccy;

    @XmlElement private String content;

    public InstdAmt() {}

    public InstdAmt(String ccy, String content) {
        this.ccy = ccy;
        this.content = content;
    }

    public ExactCurrencyAmount toAmount() {
        return ExactCurrencyAmount.of(Double.parseDouble(content), ccy);
    }
}
