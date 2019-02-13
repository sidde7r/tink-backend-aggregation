package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.xml;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.nxgen.http.URL;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlRequestListEntity {

    @XmlElement(name = "request")
    private List<XmlRequestEntity> requestList;

    public List<XmlRequestEntity> getRequestList() {
        return requestList;
    }

    public void setRequest(List<XmlRequestEntity> requestList) {
        this.requestList = requestList;
    }

    public Optional<URL> getExecuteTransferRequest() {
        return Optional.ofNullable(this.requestList)
                .map(Collection::stream)
                .flatMap(
                        requests ->
                                requests.filter(XmlRequestEntity::isExecuteTransfer).findFirst())
                .map(XmlRequestEntity::asSSORequest);
    }
}
