package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.xml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XmlParamEntity {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
