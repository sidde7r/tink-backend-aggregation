package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class InstdAmt {
    @XmlAttribute(name = "Ccy")
    private String ccy;

    @XmlValue private String content;

    public InstdAmt() {}

    public InstdAmt(String ccy, String content) {
        this.ccy = ccy;
        this.content = content;
    }

    public ExactCurrencyAmount toAmount() {
        return ExactCurrencyAmount.of(Double.parseDouble(content), ccy);
    }
}
