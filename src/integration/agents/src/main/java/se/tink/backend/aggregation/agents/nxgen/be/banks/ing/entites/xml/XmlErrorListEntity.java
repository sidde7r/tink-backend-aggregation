package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.xml;

import java.util.List;
import java.util.Optional;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlErrorListEntity {
    @XmlElement(name = "error")
    private List<XmlErrorEntity> errorList;

    public List<XmlErrorEntity> getErrorList() {
        return errorList;
    }

    public void setError(List<XmlErrorEntity> errorList) {
        this.errorList = errorList;
    }

    public Optional<String> getErrorCode() {
        return errorList != null && errorList.get(0) != null
                ? Optional.ofNullable(errorList.get(0).getCode())
                : Optional.empty();
    }
}
