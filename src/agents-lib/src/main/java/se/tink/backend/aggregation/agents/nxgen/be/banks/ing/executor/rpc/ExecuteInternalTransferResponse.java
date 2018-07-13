package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc;

import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.xml.XmlHeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "mobileResponse")
public class ExecuteInternalTransferResponse {
    private XmlHeaderEntity header;
    private String returnCode;

    public XmlHeaderEntity getHeader() {
        return header;
    }

    public void setHeader(XmlHeaderEntity header) {
        this.header = header;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }
}
