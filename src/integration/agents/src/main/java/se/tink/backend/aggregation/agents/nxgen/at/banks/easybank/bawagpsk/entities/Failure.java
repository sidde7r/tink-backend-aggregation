package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class Failure {
    private List<ResponseMessage> responseMessageList;

    @XmlElementWrapper(name = "ResponseMessageList")
    @XmlElement(name = "ResponseMessage")
    public void setResponseMessageList(List<ResponseMessage> responseMessageList) {
        this.responseMessageList = responseMessageList;
    }

    public List<ResponseMessage> getResponseMessageList() {
        return responseMessageList;
    }
}
