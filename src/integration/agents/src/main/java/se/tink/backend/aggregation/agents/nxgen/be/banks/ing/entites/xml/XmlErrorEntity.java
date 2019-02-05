package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.xml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XmlErrorEntity {
    private String code;
    private String text;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
