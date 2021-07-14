package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

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
