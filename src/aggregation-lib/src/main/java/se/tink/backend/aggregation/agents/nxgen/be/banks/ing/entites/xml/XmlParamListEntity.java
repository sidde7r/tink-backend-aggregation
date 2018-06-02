package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.xml;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlParamListEntity {

    @XmlElement(name = "param")
    private List<XmlParamEntity> paramList;

    public List<XmlParamEntity> getParamList() {
        return paramList;
    }

    public void setParam(List<XmlParamEntity> paramList) {
        this.paramList = paramList;
    }
}
